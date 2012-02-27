(ns dieter.preprocessors.hamlcoffee
  (:use dieter.preprocessors.rhino)
  (:require [clojure.string :as cstr])
  (:import [org.mozilla.javascript JavaScriptException]))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(defn format-error [e]
  (str
   "#ERROR: '" (getvar e "message") "'"
;   "IN FILE: " (getvar e "filename")
;   "LINE: " (getvar e "line") "\n"
;   (join-js-array (getvar e "extract"))
))


(defn preprocess-hamlcoffee [file]
  (with-scope ["coffee-script.js"
               "haml-coffee.js"
               "haml-coffee-assets.js"
               "haml-coffee-wrapper.js"]
    (setvar "coffeeError" nil)
    (let [input (slurp file)
          filename (filename-without-ext file)
          result (call "compileHamlCoffee" input filename)
          e (getvar "coffeeError")]
      (if e
        (throw (Exception. (format-error e)))
        result))))
