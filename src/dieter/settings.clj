(ns dieter.settings
  (:require [clojure.java.io :as io]))

(def ^:dynamic *settings* {:compress false
                           :asset-root "resources"
                           :cache-root "resources/asset-cache"
                           :cache-mode :development})

(def cached-paths (atom {}))

(defn asset-root []
  (:asset-root *settings*))

(defn absolute-asset-root []
  (.getCanonicalPath (io/file (asset-root))))

(defn cache-root []
  (:cache-root *settings*))
