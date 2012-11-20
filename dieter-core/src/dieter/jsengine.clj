(ns dieter.jsengine
  (:require [dieter.settings :as settings]
            [dieter.pools :as pools]
            [dieter.rhino :as rhino]
            [dieter.v8 :as v8]))


(defmacro with-context [& body]
  (if (= (:engine settings/*settings*) :rhino)
    `(rhino/with-context ~@body)
    `(v8/with-context ~@body)))

(defmacro with-scope [pool preloads & body]
  (if (= (:engine settings/*settings*) :rhino)
    `(rhino/with-scope ~pool ~preloads ~@body)
    `(v8/with-scope ~pool ~preloads ~@body)))

(defn call [fn-name & args]
  (if (= (:engine settings/*settings*) :rhino)
    (apply rhino/call fn-name args)
    (apply v8/call fn-name args)))

(defn run-compiler [pool preloads fn-name file]
  (let [input (slurp file)
        filename (.getCanonicalPath file)]
    (if (= (:engine settings/*settings*) :rhino)
      (rhino/with-scope pool preloads
        (rhino/call fn-name input filename))
      (v8/with-scope pool preloads
        (v8/call fn-name input filename)))))