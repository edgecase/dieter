(ns dieter.preprocessors.handlebars
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr])
  (:use dieter.preprocessors.rhino
        [dieter.settings :only [*settings*]])
  (:import [org.mozilla.javascript Context NativeObject]))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(def context (Context/enter))
(def scope (.initStandardObjects context))
(load-vendor "hbs-wrapper.js" context scope)
(load-vendor "ember-0.9.4.js" context scope)
(def ember-fn (.get scope "precompileEmber" scope))
(def handlebars-fn (.get scope "precompileHandlebars" scope))

(defn compile-ember [string filename]
  (str "Ember.TEMPLATES[\"" filename "\"]=Ember.Handlebars.template("
       (.call ember-fn context scope nil (into-array [string]))
       ");"))

(defn compile-handlebars [string filename]
  (str "Handlebars.templates = Handlebars.templates || {};"
       "Handlebars.templates[\"" filename "\"]=Handlebars.template("
       (.call handlebars-fn context scope nil (into-array [string]))
       ");"))

(defn preprocess-handlebars [file]
  (let [hbs (slurp file)
        filename (filename-without-ext file)]
    (case (:hbs-mode *settings*)
      :handlebars (compile-handlebars hbs filename)
      :ember (compile-ember hbs filename)
      :else (throw "hbs-mode not supported"))))
