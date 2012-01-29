(ns dieter.preprocessors.handlebars
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr])
  (:use [dieter.preprocessors.rhino :only [defscope call]]
        [dieter.settings :only [*settings*]])
  (:import [org.mozilla.javascript Context NativeObject]))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(defscope scope "hbs-wrapper.js" "ember-0.9.4.js")

(defn compile-ember [string filename]
  (str "Ember.TEMPLATES[\"" filename "\"]=Ember.Handlebars.template("
       (call "precompileEmber" scope string)
       ");"))

(defn compile-handlebars [string filename]
  (str "Handlebars.templates = Handlebars.templates || {};"
       "Handlebars.templates[\"" filename "\"]=Handlebars.template("
       (call "precompileHandlebars" scope string)
       ");"))

(defn preprocess-handlebars [file]
  (let [hbs (slurp file)
        filename (filename-without-ext file)]
    (case (:hbs-mode *settings*)
      :handlebars (compile-handlebars hbs filename)
      :ember (compile-ember hbs filename)
      :else (throw "hbs-mode not supported"))))
