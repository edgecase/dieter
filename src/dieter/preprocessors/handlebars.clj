(ns dieter.preprocessors.handlebars
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]
            [dieter.asset :as asset])
  (:use [dieter.preprocessors.rhino :only [with-scope call make-pool]]
        [dieter.settings :only [*settings*]])
  (:import [org.mozilla.javascript Context NativeObject]))

(defn filename-without-ext [file]
  (cstr/replace (.getName file) #"\..*$" ""))

(defn compile-ember [string filename]
  (str "Ember.TEMPLATES[\"" filename "\"]=Ember.Handlebars.template("
       (call "precompileEmber" string)
       ");"))

(defn compile-handlebars [string filename]
  (str "Handlebars.templates = Handlebars.templates || {};"
       "Handlebars.templates[\"" filename "\"]=Handlebars.template("
       (call "precompileHandlebars" string)
       ");"))

(def pool (make-pool))
(defn preprocess-handlebars [file]
  (with-scope pool ["hbs-wrapper.js" "ember-0.9.4.js"]
    (let [hbs (slurp file)
          filename (filename-without-ext file)]
      (case (:hbs-mode *settings*)
        :handlebars (compile-handlebars hbs filename)
        :ember (compile-ember hbs filename)
        :else (throw "hbs-mode not supported")))))

(defrecord Handlebars [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.Js. (:file this) (preprocess-handlebars (:file this)))))

(asset/register "hbs" map->Handlebars)
