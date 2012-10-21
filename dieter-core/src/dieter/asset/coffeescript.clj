(ns dieter.asset.coffeescript
  (:require
   dieter.asset
   dieter.asset.javascript
   [dieter.pools :as pools]
   [clojure.string :as string])
  (:use [dieter.rhino :only (call with-scope)]))

(def pool (pools/make-pool))

(defn compile-coffeescript [input filename]
  (try
    (with-scope pool ["coffee-script.js" "coffee-wrapper.js"]
      (str (call "compileCoffeeScript" input filename)))
    (catch org.mozilla.javascript.JavaScriptException e
      (format "throw(\"%s\")" (string/replace (.getMessage e) "\"" "\\\"" )))))

(defn preprocess-coffeescript [file]
  (compile-coffeescript (slurp file) (.getCanonicalPath file)))

(defrecord Coffee [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.javascript.Js. (:file this) (preprocess-coffeescript (:file this)))))
