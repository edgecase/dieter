(ns dieter.rhino
  (:require [clojure.java.io :as io])
  (:import [org.mozilla.javascript Context NativeObject]))


;;; Pool of scopes, to be used one per preprocessor

(defn make-pool []
  (ref #{}))

(defn remove-from-pool [pool item]
  (alter pool disj item)
  item)

(defn get-from-pool [pool initializer]
  (let [a (atom nil)]
    (dosync
     (when-let [item (first @pool)]
       (swap! a (constantly item))
       (remove-from-pool pool item)))
    (if @a
      @a
      (initializer))))

(defn add-to-pool [pool item]
  (dosync
   (alter pool conj item)))

(defmacro with-pool [pool name initializer & body]
  `(let [~name (get-from-pool ~pool ~initializer)]
     (try
       (do ~@body)
       (finally (add-to-pool ~pool ~name)))))




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
     (with-pool ~pool ~'pool-entry #(new-scope ~preloads)
       (binding [scope ~'pool-entry]
         ~@body))))



(defn call [fn-name & args]
  (let [fun (.get scope fn-name scope)]
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