(ns dieter.preprocessors.coffeescript
  (:use [dieter.preprocessors.rhino :only (call with-scope make-pool)]))

(def pool (make-pool))

(defn compile-coffeescript [input]
  (with-scope pool ["coffee-script.js" "coffee-wrapper.js"]
    (str (call "compileCoffeeScript" input))))

(defn preprocess-coffeescript [file]
  (compile-coffeescript (slurp file)))
