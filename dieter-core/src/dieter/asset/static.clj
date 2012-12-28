(ns dieter.asset.static
  (:require [dieter.asset :as asset]))

(defrecord Static [file content]
  dieter.asset.Asset
  (read-asset [this]
    (assoc this :content
           (with-open [in (java.io.BufferedInputStream. (java.io.FileInputStream. (:file this)))]
             (let [buf (make-array Byte/TYPE (.length (:file this)))]
               (.read in buf)
               buf))))

  dieter.asset.Compressor
  (compress [this]
    (:content this)))

(asset/register :default map->Static)