(ns dieter.asset.sass
  (:require [dieter.asset :as asset]
            [dieter.pools :as pools]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:use 
    [dieter.util :only [slurp-into string-builder]]))

(def engine 
  (.getEngineByName 
    (new javax.script.ScriptEngineManager) 
    "jruby"))

(.eval engine (str "$LOAD_PATH.unshift '" (str/replace (io/resource "vendor/sass/lib") "file:" "") "'"))
(.eval engine "['rubygems', 'sass'].each {|x| require x}")

(def sassEngine (.eval engine "Sass"))
(def rubyEngine (org.jruby.Ruby/getGlobalRuntime))

(def sass-option-hash (.eval engine "{:cache => false, :syntax => :sass}"))
(def scss-option-hash (.eval engine "{:cache => false, :syntax => :scss}"))

(defn method-param-list [text options]
  (into-array org.jruby.runtime.builtin.IRubyObject
    [(org.jruby.RubyString/newString rubyEngine text) options]))

(defn compress-sass [text]
  (.callMethod sassEngine 
               (.getCurrentContext rubyEngine) 
               "compile"
               (method-param-list text sass-option-hash)))

(defn compress-scss [text]
  (.callMethod sassEngine 
               (.getCurrentContext rubyEngine) 
               "compile"
               (method-param-list text scss-option-hash)))

(defrecord Sass [file content]
  dieter.asset.Asset
  (read-asset [this]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (:file this) " */\n")
            (:file this))))

  dieter.asset.Compressor
  (compress [this]
    (compress-sass (:content this))))

(defrecord Scss [file content]
  dieter.asset.Asset
  (read-asset [this]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (:file this) " */\n")
            (:file this))))

  dieter.asset.Compressor
  (compress [this]
    (compress-sass (:content this))))

(asset/register "sass" map->Sass)
(asset/register "scss" map->Scss)
