(ns dieter.compressor
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr])
  (:import [com.google.javascript.jscomp JSSourceFile CompilerOptions CompilationLevel]))

(defn compress-js [text]
  (let [compiler (com.google.javascript.jscomp.Compiler.)
        options (CompilerOptions.)]
    (.setOptionsForCompilationLevel (CompilationLevel/SIMPLE_OPTIMIZATIONS) options)
    (.compile compiler (make-array JSSourceFile 0)
              (into-array JSSourceFile [(JSSourceFile/fromCode "awesome-js" text)])
              options)
    (.toSource compiler)))

(defn compress-css [text]
  (cstr/replace (cstr/replace text "\n" "") #"\s+" " "))