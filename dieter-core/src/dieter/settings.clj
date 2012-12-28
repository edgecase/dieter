(ns dieter.settings
  (:require [clojure.java.io :as io]))

(defonce ^:dynamic *settings*
  {:engine     :rhino
   :compress   false
   :asset-root "resources"
   :cache-root "resources/asset-cache"
   :cache-mode :development
   :log-level  :normal})

(defmacro with-options [options & body]
  `(binding [*settings* (merge *settings* ~options)]
     (do ~@body)))

(defn asset-roots []
  (or
    (:asset-roots *settings*)  ;; Plural
    (when-let [ar (:asset-root *settings*)] [ar]))) ;; Fallback

(defn cache-root []
  (:cache-root *settings*))

(defn precompiles []
  (:precompiles *settings*))

(defn log-level []
  (:log-level *settings*))

(defn compress? []
  (-> *settings* :compress boolean))

(defn production? []
  (-> *settings* :cache-mode (= :development) not))