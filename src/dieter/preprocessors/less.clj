(ns dieter.preprocessors.less
  (:use dieter.preprocessors.rhino)
  (:require [clojure.string :as cstr])
  (:import [org.mozilla.javascript JavaScriptException]))

(defn join-js-array [a]
  (let [len (.getLength a)]
    (if (< 0 len)
      (cstr/join "\n" (for [n (range 0 len)]
                        (.get a n scope)))
      "")))

(defn format-error [e]
  (str
   "/*\nERROR: " (getvar e "message") "\n"
   "IN FILE: " (getvar e "filename") "\n"
   "LINE: " (getvar e "line") "\n"
   (join-js-array (getvar e "extract"))
   "\n*/\n"))

(defn preprocess-less [file]
  (with-scope ["less-wrapper.js" "less-rhino-1.2.1.js"]
    (setvar "lessResult" "")
    (setvar "lessError" nil)
    (call "compileLess" (.getCanonicalPath file))
    (if-let [e (getvar "lessError")]
      (throw (Exception. (format-error e)))
      (getvar "lessResult"))))
