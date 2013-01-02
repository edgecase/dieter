(ns dieter.asset.javascript
  (:require [dieter.asset :as asset]
            [dieter.settings :as settings])
  (:use [dieter.util :only [slurp-into string-builder]])
  (:import [com.google.javascript.jscomp JSSourceFile CompilerOptions CompilationLevel WarningLevel]
           [java.util.logging Logger Level]))

;; TODO: use pools here too to avoid constructor overhead
(defn make-compiler []
  (let [compiler (com.google.javascript.jscomp.Compiler.)
        options (CompilerOptions.)]
    (.setOptionsForCompilationLevel (CompilationLevel/SIMPLE_OPTIMIZATIONS) options)
    (if (= :quiet (settings/log-level))
      (do
        (.setOptionsForWarningLevel (WarningLevel/QUIET) options)
        (.setLevel (Logger/getLogger "com.google.javascript.jscomp") Level/OFF))
      (do
        (.setOptionsForWarningLevel (WarningLevel/VERBOSE) options)
        (.setLevel (Logger/getLogger "com.google.javascript.jscomp") Level/WARNING)))
    [compiler options]))

(defn compress-js [filename text]
  (let [[compiler options] (make-compiler)]
    (.compile compiler
              (make-array JSSourceFile 0)
              (into-array JSSourceFile [(JSSourceFile/fromCode (str filename) (str text))])
              options)
    (let [source (.toSource compiler)]
      (if (.isEmpty source)
        text
        source))))

(defrecord Js [file content]
  dieter.asset.Asset
  (read-asset [this]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (:file this) " */\n")
            (:file this))))

  dieter.asset.Compressor
  (compress [this]
    (compress-js (:file this) (:content this))))

(asset/register "js" map->Js)