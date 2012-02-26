(ns dieter.preprocessors.hamlcoffee
  (:use dieter.preprocessors.rhino)
  (:require [clojure.string :as cstr])
  (:import [org.mozilla.javascript JavaScriptException]))

(defscope scope "coffee-script.js" "haml-coffee.js" "haml-coffee-assets.js" "haml-coffee-wrapper.js")

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(defn format-error [e]
  (str
   "#ERROR: '" (getvar e "message" scope) "'"
;   "IN FILE: " (getvar e "filename" scope)
;   "LINE: " (getvar e "line" scope) "\n"
;   (join-js-array (getvar e "extract" scope))
))


(defn preprocess-hamlcoffee [file]
  (setvar scope "coffeeError" nil)
  (let [input (slurp file)
        filename (filename-without-ext file)
        result (call "compileHamlCoffee" scope input filename)
        e (getvar scope "coffeeError")]
    (if e
      (throw (Exception. (format-error e)))
      result)))
