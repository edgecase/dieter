(ns dieter.preprocessors.coffeescript
  (:use [dieter.preprocessors.rhino :only (call with-scope make-pool)]))

(def pool (make-pool))

(defn compile-coffeescript [input filename]
  (with-scope pool ["coffee-script.js" "coffee-wrapper.js"]
    (str (call "compileCoffeeScript" input filename))))

(defn preprocess-coffeescript [file]
  (compile-coffeescript (slurp file) (.getCanonicalPath file)))
