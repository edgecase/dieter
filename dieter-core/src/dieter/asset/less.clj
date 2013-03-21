(ns dieter.asset.less
  (:require [dieter.pools :as pools]
            dieter.asset.css
            [dieter.settings :as settings]
            [dieter.asset :as asset])
  (:use [dieter.jsengine :only (run-compiler)]))

(def pool (pools/make-pool))

(defn preprocess-less [file]
  (run-compiler pool
                (if (-> settings/*settings* :engine (= :rhino))
                  ["less-rhino-wrapper.js" "less-wrapper.js" "less-rhino-1.3.3.js"]
                  ["less-wrapper.js" "less-rhino-1.3.3.js"])
                (if (settings/compress?) "compileLessCompress" "compileLessNoCompress")
                file))

(defrecord Less [file]
  dieter.asset.Asset
  (read-asset [this]
    (dieter.asset.css.Css. (:file this) (preprocess-less (:file this)))))

(asset/register "less" map->Less)
