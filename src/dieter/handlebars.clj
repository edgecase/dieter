(ns dieter.handlebars
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr])
  (:import [org.mozilla.javascript Context Scriptable]))

(defn eval-with-handlebars [string]
  (let [hbs-file (io/reader "vendor/handlebars-1.0.0.beta.6.js")
        context (Context/enter)
        scope (.initStandardObjects context)
        handlebars (.evaluateReader context scope hbs-file "handlebars" 1 nil)
        results (.evaluateString context scope string "template" 1 nil)]
    (Context/exit)
    results))

(defn quote-js [s]
  (str "\\x22" (cstr/replace s "\"" "\\x22") "\\x22"))

(defn preprocess-handlebars [file]
  (let [hbs (slurp file)
        js  (str "Handlebars.precompile(" (quote-js hbs) ");")
        compiled (eval-with-handlebars js)]
    (println js)
    (str "DIETER_HANDLEBARS_TEMPLATES[" (.getName file) "]=" compiled)))
