(ns dieter.asset.coffeescript
  (:require
   dieter.asset
   dieter.asset.javascript
   [dieter.pools :as pools])
  (:use [dieter.jsengine :only (call with-scope run-compiler)]))

(def pool (pools/make-pool))

(defn compile-coffeescript [input file]
  (run-compiler pool ["coffee-script.js" "coffee-wrapper.js"]
                "compileCoffeeScript" input file))

(defn preprocess-coffeescript [file]
  (compile-coffeescript (slurp file) (.getCanonicalPath file)))

(defrecord Coffee [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.javascript.Js. (:file this) (preprocess-coffeescript (:file this)))))
