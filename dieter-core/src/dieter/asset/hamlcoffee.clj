(ns dieter.asset.hamlcoffee
  (:require [dieter.pools :as pools]
            dieter.asset.javascript
            [clojure.string :as cstr])
  (:use [dieter.rhino :only (with-scope call)]))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(def pool (pools/make-pool))

(defn preprocess-hamlcoffee [file]
  (with-scope pool ["coffee-script.js"
                    ;; imported direct from https://raw.github.com/netzpirat/haml-coffee/master/dist/compiler/hamlcoffee.js
                    "hamlcoffee.js"
                    "hamlcoffee-assets-rhino-fix.js"
                    ;; imported direct from https://raw.github.com/netzpirat/haml_coffee_assets/master/lib/js/haml_coffee_assets.js
                    "haml_coffee_assets.js"
                    "hamlcoffee-wrapper.js"]
    (let [input (slurp file)
          filename (filename-without-ext file)]
      (call "compileHamlCoffee" input filename))))

(defrecord HamlCoffee [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.javascript.Js. (:file this) (preprocess-hamlcoffee (:file this)))))
