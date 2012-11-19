(ns dieter.asset.less
  (:use
   [dieter.jsengine :only (with-scope call)])
  (:require
   dieter.asset.css
   [dieter.pools :as pools]
   [clojure.string :as cstr]
   [dieter.asset :as asset]))

(def pool (pools/make-pool))

(defn preprocess-less [file]
  (with-scope pool ["less-wrapper.js" "less-rhino-1.3.0.js"]
    (call "compileLess" (.getCanonicalPath file))))

(defrecord Less [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.css.Css. (:file this) (preprocess-less (:file this)))))
