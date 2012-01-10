(ns dieter.handlebars
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]
            [clojure.reflect])
  (:import [org.mozilla.javascript Context Scriptable]))

(defn compile-handlebars [string]
  (let [hbs-file (io/reader "vendor/handlebars-1.0.0.beta.6.js")
        context (Context/enter)
        scope (.initStandardObjects context)
        _ (.evaluateReader context scope hbs-file "handlebars" 1 nil)
        handlebars (.get scope "Handlebars" scope)
        compiler   (.get handlebars "precompile" scope)
        results (.call compiler context scope nil (into-array [string]))]
    (Context/exit)
    results))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(defn preprocess-handlebars [file]
  (let [hbs (slurp file)
        compiled (compile-handlebars hbs)]
    (str "var DIETER_HANDLEBARS_TEMPLATES = DIETER_HANDLEBARS_TEMPLATES || {};\n"
     "DIETER_HANDLEBARS_TEMPLATES[\"" (filename-without-ext file) "\"]=" compiled)))

