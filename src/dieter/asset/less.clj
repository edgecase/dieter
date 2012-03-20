(ns dieter.asset.less
  (:use
   [dieter.asset :only [register]]
   [dieter.rhino :only (with-scope make-pool call)])
  (:require
   dieter.asset.css
   [clojure.string :as cstr]
   [dieter.asset :as asset]))

(def pool (make-pool))

(defn preprocess-less [file]
  (with-scope pool ["less-wrapper.js" "less-rhino-1.2.1.js"]
    (call "compileLess" (.getCanonicalPath file))))

(defrecord Less [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.css.Css. (:file this) (preprocess-less (:file this)))))

(register "less" map->Less)