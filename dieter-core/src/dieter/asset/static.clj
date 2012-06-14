(ns dieter.asset.static)

(defrecord Static [file content]
  dieter.asset.Asset
  (read-asset [this options]
    (assoc this :content
           (with-open [in (java.io.BufferedInputStream. (java.io.FileInputStream. (:file this)))]
             (let [buf (make-array Byte/TYPE (.length (:file this)))]
               (.read in buf)
               buf))))

  dieter.asset.Compressor
  (compress [this options]
    (:content this)))
