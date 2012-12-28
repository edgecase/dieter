(ns dieter.asset.css
  (:require [dieter.asset :as asset]
            [dieter.settings :as settings])
  (:use [dieter.util :only [slurp-into string-builder]])
  (:require [clojure.string :as s]))

(defn compress-css [text]
  (-> text
      (s/replace "\n" "")
      (s/replace #"\s+" " ")
      (s/replace #"^\s" "")))

(defrecord Css [file content]
  dieter.asset.Asset
  (read-asset [this]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (:file this) " */\n")
            (:file this))))

  dieter.asset.Compressor
  (compress [this]
    (if (settings/compress?)
      (compress-css (:content this))
      (:content this))))

(asset/register "css" map->Css)