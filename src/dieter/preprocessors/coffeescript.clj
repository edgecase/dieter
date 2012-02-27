(ns dieter.preprocessors.coffeescript
  (:use dieter.preprocessors.rhino))

(defn compile-coffeescript [input]
  (with-scope ["coffee-script.js" "coffee-wrapper.js"]
    (str (call "compileCoffeeScript" input))))

(defn preprocess-coffeescript [file]
  (compile-coffeescript (slurp file)))
