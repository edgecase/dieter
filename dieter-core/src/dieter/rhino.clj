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



(defn call [fn-name & args]
  (let [#^org.mozilla.javascript.InterpretedFunction fun (.get scope fn-name scope)]
    (.call fun context scope nil (into-array args))))

(defn getvar
  ([name]
     (getvar name scope))
  ([name obj]
     (.get scope name obj)))

(defn setvar [name val]
  (.put scope name scope val))

(defn load-vendor [filename scope]
  (.evaluateReader context scope
                   (io/reader (io/resource (str "vendor/" filename)))
                   filename 1 nil))

(defn js-keys [obj]
  (seq (NativeObject/getPropertyIds obj)))