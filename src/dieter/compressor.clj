(ns dieter.compressor
  (:require [clojure.java.io :as io])
  (:import [com.yahoo.platform.yui.compressor JavaScriptCompressor CssCompressor]
           [org.mozilla.javascript ErrorReporter]))

(def reporter (reify ErrorReporter
                (warning [this message source-name line line-source line-offset] (println message))
                (error [this message source-name line line-source line-offset] (println message))
                (runtimeError [this message source-name line line-source line-offset] (Exception. message))))

; can this come in as a string, or always a file? Where does the
; decision to compress as js or css made?
;(defn compress [input-file output-file level])

(defn compress-js [input-file output-file level]
  (with-open [infile (io/reader input-file)
              outfile (io/writer output-file)]
    (let [compressor (JavaScriptCompressor. infile reporter)]
      (case level
        :debug (.compress compressor outfile
                          -1    ; line break
                          false ; munge (shorten var names)
                          true  ; verbose
                          true  ; preserve all semicolons
                          true  ; disable optimizations
                          )
        :minified (.compress compressor outfile
                             -1    ; line break
                             true  ; munge (shorten var names)
                             false ; verbose
                             false ; preserve all semicolons
                             false ; disable optimizations
                             )))))

(defn compress-css [input-file output-file]
  (with-open [infile (io/reader input-file)
              outfile (io/writer output-file)]
    (let [compressor (CssCompressor. infile)]
      (.compress compressor outfile -1)))))