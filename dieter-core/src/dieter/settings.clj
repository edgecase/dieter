(ns dieter.settings
  (:require [clojure.java.io :as io]))

(defonce ^:dynamic *settings*
  {:compress   false
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

(defn asset-root []
  (:asset-root *settings*))

(defn absolute-asset-root []
  (.getCanonicalPath (io/file (asset-root))))

(defn cache-root []
  (:cache-root *settings*))