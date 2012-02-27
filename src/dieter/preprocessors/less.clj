(ns dieter.preprocessors.less
  (:use [dieter.preprocessors.rhino :only (with-scope make-pool call)])
  (:require [clojure.string :as cstr])
  (:import [org.mozilla.javascript JavaScriptException]))

(def pool (make-pool))
(defn preprocess-less [file]
  (with-scope pool ["less-wrapper.js" "less-rhino-1.2.1.js"]
    (call "compileLess" (.getCanonicalPath file))))
