(ns dieter.asset.less
  (:require
   dieter.asset.css
   [dieter.pools :as pools]
   [clojure.string :as cstr]
   [dieter.asset :as asset])
  (:use [dieter.jsengine :only (run-compiler)]))

(def pool (pools/make-pool))

(defn preprocess-less [file]
  (run-compiler pool
                ["less-wrapper.js" "less-rhino-1.3.0.js"]
                "compileLess"
                file
                :engine :rhino)) ; cant support v8 imports yet

(defrecord Less [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.css.Css. (:file this) (preprocess-less (:file this)))))
