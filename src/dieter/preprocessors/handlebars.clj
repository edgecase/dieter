(ns dieter.preprocessors.handlebars
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr])
  (:use dieter.preprocessors.rhino
        [dieter.settings :only [*settings*]]))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(defn compile-ember [string filename]
  (with-rhino context scope
    (load-vendor "hbs-wrapper.js" context scope)
    (load-vendor "ember-0.9.4.js" context scope)
    (let [compiler (.get scope "precompileEmber" scope)
          func (.call compiler context scope nil (into-array [string]))]
      (str "Ember.TEMPLATES[\"" filename "\"]=Ember.Handlebars.template(" func ");"))))

(defn compile-handlebars [string filename]
  (with-rhino context scope
    (load-vendor "hbs-wrapper.js" context scope)
    (load-vendor "handlebars-1.0.0.beta.6.js" context scope)
    (let [compiler (.get scope "precompileHandlebars" scope)
          func (.call compiler context scope nil (into-array [string]))]
      (str "Handlebars.templates = Handlebars.templates || {};"
           "Handlebars.templates[\"" filename "\"]=Handlebars.template(" func ");"))))

(defn preprocess-handlebars [file]
  (let [hbs (slurp file)
        filename (filename-without-ext file)]
    (case (:hbs-mode *settings*)
      :handlebars (compile-handlebars hbs filename)
      :ember (compile-ember hbs filename)
      :else (throw "hbs-mode not supported"))))



