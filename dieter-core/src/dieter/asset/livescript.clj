(ns dieter.asset.livescript
  (:require [dieter.asset :as asset]
            dieter.asset.javascript
            [dieter.pools :as pools])
  (:use [dieter.jsengine :only (run-compiler)]))

(def pool (pools/make-pool))

(defn compile-livescript [file]
  (run-compiler pool
                ["livescript.js" "ls-wrapper.js"]
                "compileLiveScript"
                file))

(defn preprocess-livescript [file]
  (asset/memoize-file file compile-livescript))

(defrecord LiveScript [file]
  dieter.asset.Asset
  (read-asset [this]
    (dieter.asset.javascript.Js. (:file this) (preprocess-livescript (:file this)))))

(asset/register "ls" map->LiveScript)
