(ns dieter.asset.coffeescript
  (:require [dieter.asset :as asset]
            dieter.asset.javascript
            [dieter.pools :as pools])
  (:use [dieter.jsengine :only (run-compiler)]))

(def pool (pools/make-pool))

(defn preprocess-coffeescript [file]
  (asset/memoize-file file
                      #(run-compiler pool
                                     ["coffee-script.js" "coffee-wrapper.js"]
                                     "compileCoffeeScript"
                                     file)))

(defrecord Coffee [file]
  dieter.asset.Asset
  (read-asset [this]
    (dieter.asset.javascript.Js. (:file this) (preprocess-coffeescript (:file this)))))

(asset/register "coffee" map->Coffee)
(asset/register "cs" map->Coffee)