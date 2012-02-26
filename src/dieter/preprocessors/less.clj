(ns dieter.preprocessors.less
  (:use dieter.preprocessors.rhino)
  (:require [clojure.string :as cstr])
  (:import [org.mozilla.javascript JavaScriptException]))

(defscope scope "less-wrapper.js" "less-rhino-1.2.1.js")

(defn join-js-array [a]
  (let [len (.getLength a)]
    (if (< 0 len)
      (cstr/join "\n" (for [n (range 0 len)]
                        (.get a n scope)))
      "")))

(defn format-error [e]
  (str
   "/*\nERROR: " (getvar e "message" scope) "\n"
   "IN FILE: " (getvar e "filename" scope) "\n"
   "LINE: " (getvar e "line" scope) "\n"
   (join-js-array (getvar e "extract" scope))
   "\n*/\n"))

(defn preprocess-less [file]
  (setvar scope "lessResult" "")
  (setvar scope "lessError" nil)
  (call "compileLess" scope (.getCanonicalPath file))
  (if-let [e (getvar scope "lessError")]
    (throw (Exception. (format-error e)))
    (getvar scope "lessResult")))
