(ns dieter.asset.hamlcoffee
  (:use
   dieter.rhino
   [dieter.asset :only [register]])
  (:require
   dieter.asset.javascript
   [clojure.string :as cstr]))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(def pool (make-pool))

(defn preprocess-hamlcoffee [file]
  (with-scope pool ["coffee-script.js"
                    "haml-coffee.js"
                    "haml-coffee-assets.js"
                    "haml-coffee-wrapper.js"]
    (let [input (slurp file)
          filename (filename-without-ext file)]
      (call "compileHamlCoffee" input filename))))

(defrecord HamlCoffee [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.javascript.Js. (:file this) (preprocess-hamlcoffee (:file this)))))

(register "hamlc" map->HamlCoffee)