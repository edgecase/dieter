(ns dieter.rhino
  (:require [clojure.java.io :as io]
            [dieter.pools :as pools])
  (:import [org.mozilla.javascript Context NativeObject]))



(def ^:dynamic context nil)
(def ^:dynamic scope nil)

(declare load-vendor)
(defn new-scope [preloads]
  (let [scope (.initStandardObjects context)]
    (doseq [file preloads]
      (load-vendor file scope))
    scope))

(defmacro with-context [& body]
  `(binding [context (Context/enter)]
     (.setOptimizationLevel context -1) ; Rhino hits a 64K limit when compiling
                                        ; coffeescript without this
     (try ~@body
          (finally (Context/exit)))))


(defmacro with-scope [pool preloads & body]
  `(with-context
     (pools/with-pool ~pool ~'pool-entry #(new-scope ~preloads)
       (binding [scope ~'pool-entry]
         ~@body))))

;; TODO We'd like to make sure that exceptions get reported as clojure hash, not
;; as "[object Error]". When that happens in both Rhino and v8, we can remove
;; the formatError wrappers in the various compiler wrappers
(defn jsobj->map [jsobj]
  (if (instance? org.mozilla.javascript.ScriptableObject jsobj)
    (into {}
          (doseq [id (.getAllIds jsobj)]
            [(keyword id) (if (instance? java.lang.String id)
                            (.get jsobj (cast java.lang.String id) jsobj)
                            (throw "try again"))]))
    jsobj))

(defmacro catch-carefully [& body]
  `(try
     ~@body
     (catch org.mozilla.javascript.JavaScriptException e#
       (let [script-trace# (.getScriptStackTrace e#)
             message# (str (-> e# .getMessage jsobj->map))
             new-exc# (java.lang.Exception. (str message# "\n" script-trace#))]
         (.setStackTrace new-exc# (.getStackTrace e#))
         (throw new-exc#)))))

(defn call [fn-name & args]
  (catch-carefully
   (let [#^org.mozilla.javascript.InterpretedFunction fun (.get scope fn-name scope)]
     (.call fun context scope nil (into-array args)))))

(defn load-vendor [filename scope]
  (catch-carefully
   (.evaluateReader context scope
                    (io/reader (io/resource (str "vendor/" filename)))
                    filename 1 nil)))