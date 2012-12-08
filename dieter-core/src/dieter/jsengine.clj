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

;; TODO: take an asset to avoid slurping here
(defn run-compiler [pool preloads fn-name file & {:keys [engine]}]
  (let [input (slurp file)
        absolute (.getAbsolutePath file)
        filename (.getCanonicalPath file)
        args [input absolute filename]]
    (if (or (= (:engine settings/*settings*) :rhino)
            (= engine :rhino))
      (rhino/with-scope pool preloads
        (rhino/call fn-name args))
      (v8/with-scope pool preloads
        (v8/call fn-name args)))))