(ns dieter.preprocessors.rhino
  (:require [clojure.java.io :as io])
  (:import [org.mozilla.javascript Context NativeObject]))

(def ^:dynamic context nil)
(def ^:dynamic scope nil)

(defmacro with-context [& body]
  `(binding [context (Context/enter)]
     (.setOptimizationLevel context -1) ; Rhino hits a 64K limit when compiling
                                        ; coffeescript without this
     (try ~@body
          (finally (Context/exit)))))

(defmacro with-scope [preloads & body]
  `(with-context
     (binding [scope (.initStandardObjects context)]
       (doseq [file# '~preloads]
         (load-vendor file#))
       (do ~@body))))

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

(defn load-vendor [filename]
  (.evaluateReader context scope
                   (io/reader (io/resource (str "vendor/" filename)))
                   filename 1 nil))

(defn js-keys [obj]
  (seq (NativeObject/getPropertyIds obj)))