(ns dieter.asset.css
  (:require [dieter.asset :as asset])
  (:use [dieter.util :only [slurp-into string-builder]])
  (:require [clojure.string :as s]))

(defn compress-css [text]
  (-> text
      (s/replace "\n" "")
      (s/replace #"\s+" " ")
      (s/replace #"^\s" "")))

(defrecord Css [file content]
  dieter.asset.Asset
  (read-asset [this options]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (:file this) " */\n")
            (:file this))))

  dieter.asset.Compressor
  (compress [this options]
    (if (:compress options)
      (compress-css (:content this))
      (:content this))))

(asset/register "css" map->Css)