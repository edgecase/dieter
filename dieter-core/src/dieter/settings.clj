(ns dieter.settings
  (:require [clojure.java.io :as io]))

(defonce ^:dynamic *settings*
  {:engine     :rhino
   :compress   false
   :asset-root "resources"
   :cache-root "resources/asset-cache"
   :cache-mode :development
   :hbs-mode   :handlebars
   :log-level  :normal})

(defmacro with-options [options & body]
  `(binding [*settings* (merge *settings* ~options)]
     (do ~@body)))

(defonce cached-paths (atom {}))

(defn add-cached-path [path new-path]
  (swap! cached-paths assoc path new-path))

(defn asset-roots []
  (or
    (:asset-roots *settings*)  ;; Plural
    (when-let [ar (:asset-root *settings*)] [ar]))) ;; Fallback

(defn- abs-path [path]
  (.getCanonicalPath (io/file path)))

(defn absolute-asset-roots []
  (map abs-path (asset-roots)))

(defn cache-root []
  (:cache-root *settings*))
