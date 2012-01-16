(ns dieter.preprocessors.handlebars
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr])
  (:use dieter.preprocessors.rhino))

(defn compile-handlebars [string]
  (with-rhino context scope
    (load-vendor "handlebars-1.0.0.beta.6.js" context scope)
    (let [handlebars (.get scope "Handlebars" scope)
          compiler   (.get handlebars "precompile" scope)]
      (.call compiler context scope nil (into-array [string])))))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(defn preprocess-handlebars [file]
  (let [hbs (slurp file)
        compiled (compile-handlebars hbs)]
    (str "var DIETER_HANDLEBARS_TEMPLATES = DIETER_HANDLEBARS_TEMPLATES || {};\n"
         "DIETER_HANDLEBARS_TEMPLATES[\"" (filename-without-ext file) "\"]=" compiled)))

