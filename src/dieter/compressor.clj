(ns dieter.compressor
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr])
  (:import [com.google.javascript.jscomp JSSourceFile CompilerOptions CompilationLevel WarningLevel]
           [java.util.logging Logger Level]))

(defn compress-js [text]
  (let [compiler (com.google.javascript.jscomp.Compiler.)
        options (CompilerOptions.)]
    (.setOptionsForCompilationLevel (CompilationLevel/SIMPLE_OPTIMIZATIONS) options)
    (.setOptionsForWarningLevel (WarningLevel/QUIET) options)
    (.setLevel (Logger/getLogger "com.google.javascript.jscomp") Level/OFF)
    (.compile compiler (make-array JSSourceFile 0)
              (into-array JSSourceFile [(JSSourceFile/fromCode "awesome-js" (str text))])
              options)
    (let [source (.toSource compiler)]
      (if (seq source) source text))))

(defn compress-css [text]
  (-> text
      (cstr/replace "\n" "")
      (cstr/replace #"\s+" " ")
      (cstr/replace #"^\s" "")))