(ns dieter.asset.less
  (:use
   [dieter.rhino :only (with-scope call)])
  (:require
   dieter.asset.css
   [dieter.pools :as pools]
   [clojure.string :as cstr]
   [dieter.asset :as asset]
   [clojure.string :as string]))

(def pool (pools/make-pool))

(defn sanitize-css-content [str]
  (string/replace
   (string/replace
    str
    "\"" "\\\"" )
   "\n" "\\A"))

(defn preprocess-less [file]
  (let [filename (.getCanonicalPath file)]
    (try
      (with-scope pool ["less-wrapper.js" "less-rhino-1.3.0.js"]
        (call "compileLess" filename))
      (catch org.mozilla.javascript.JavaScriptException e
        (format "body:before { content: \"In file %s - %s\"; white-space: pre }"
                filename
                (sanitize-css-content (.getMessage e)))))))

(defrecord Less [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.css.Css. (:file this) (preprocess-less (:file this)))))
