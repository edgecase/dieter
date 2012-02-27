(ns dieter.preprocessors.hamlcoffee
  (:use dieter.preprocessors.rhino)
  (:require [clojure.string :as cstr])
  (:import [org.mozilla.javascript JavaScriptException]))

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
