(ns dieter.preprocessors.coffeescript
  (:use dieter.preprocessors.rhino))

(defscope scope "coffee-script.js" "coffee-wrapper.js")

(defn compile-coffeescript [input]
  (str (call "compileCoffeeScript" scope input)))

(defn preprocess-coffeescript [file]
  (compile-coffeescript (slurp file)))
