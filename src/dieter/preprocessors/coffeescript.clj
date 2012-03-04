(ns dieter.preprocessors.coffeescript
  (:use [dieter.preprocessors.rhino :only (call with-scope make-pool)])
  (:require [dieter.asset :as asset]))

(def pool (make-pool))

(defn compile-coffeescript [input filename]
  (with-scope pool ["coffee-script.js" "coffee-wrapper.js"]
    (str (call "compileCoffeeScript" input filename))))

(defn preprocess-coffeescript [file]
  (compile-coffeescript (slurp file) (.getCanonicalPath file)))

(defrecord Coffee [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.Js. (:file this) (preprocess-coffeescript (:file this)))))

(asset/register "coffee" map->Coffee)
(asset/register "cs" map->Coffee)