(ns dieter.compressor
  (:require [clojure.java.io :as io])
  (:import [java.io StringReader StringWriter]
           [com.yahoo.platform.yui.compressor JavaScriptCompressor CssCompressor]
           [org.mozilla.javascript ErrorReporter]))

(def reporter (reify ErrorReporter
                (warning [this message source-name line line-source line-offset] (println message))
                (error [this message source-name line line-source line-offset] (println message))
                (runtimeError [this message source-name line line-source line-offset] (Exception. message))))

; can this come in as a string, or always a file? Where does the
; decision to compress as js or css made?
;(defn compress [input-file output-file level])

(defn compress-js [text]
  (let [input (StringReader. text)
        output (StringWriter.)
        compressor (JavaScriptCompressor. input reporter)]
    (.compress compressor output
               -1    ; line break
               true  ; munge (shorten var names)
               false ; verbose
               false ; preserve all semicolons
               false ; disable optimizations
               )
    (str output)))

(defn compress-css [text]
  (with-open [input (StringReader. text)
              output (StringWriter.)]
    (let [compressor (CssCompressor. input)]
      (.compress compressor output -1)
      (str output))))