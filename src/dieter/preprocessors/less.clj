(ns dieter.preprocessors.less
  (:use [dieter.preprocessors.rhino :only (with-scope make-pool call)])
  (:require [clojure.string :as cstr]
            [dieter.asset :as asset])
  (:import [org.mozilla.javascript JavaScriptException]))

(def pool (make-pool))

(defn preprocess-less [file]
  (with-scope pool ["less-wrapper.js" "less-rhino-1.2.1.js"]
    (call "compileLess" (.getCanonicalPath file))))

(defrecord Less [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.Css. (:file this) (preprocess-less (:file this)))))

(asset/register "less" map->Less)