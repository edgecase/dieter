(ns dieter.preprocessors.hamlcoffee
  (:use dieter.preprocessors.rhino)
  (:require [clojure.string :as cstr])
  (:import [org.mozilla.javascript JavaScriptException]))

(defscope scope "coffee-script.js" "haml-coffee.js" "haml-coffee-assets.js" "haml-coffee-wrapper.js")

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(defn compile-hamlcoffee [input filename]
  (str (call "compileHamlCoffee" scope input filename)))

(defn preprocess-hamlcoffee [file]
  (let [input (slurp file)
        filename (filename-without-ext file)]
    (compile-hamlcoffee input filename)))
