(ns dieter.asset
  (:use
   [dieter.util :only [slurp-into string-builder]]
   [dieter.path :only [file-ext]]
   [dieter.compressor :only [compress-js compress-css]]))

(defprotocol Asset
  "Protocol for pre-processing assets"
  (read-asset [this options]
    "Perform all pre-processing on the object. Must return an Asset."))

(defprotocol Compressor
  "Protocol for compressing assets"
  (compress [this options]
    "Perform any required compression / minification.
Must return final contents of the file for output.
Contents can be a String, StringBuilder, or byte[]"))

(defrecord Js [file content]
  Asset
  (read-asset [this options]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (:file this) " */\n")
            (:file this))))
  Compressor
  (compress [this options]
    (if (:compress options)
      (compress-js (:content this))
      (:content this))))

(defrecord Css [file content]
  Asset
  (read-asset [this options]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (.getCanonicalPath (:file this)) " */\n")
            (:file this))))
  Compressor
  (compress [this options]
    (if (:compress options)
      (compress-css (:content this))
      (:content this))))

(defrecord Static [file content]
  Asset
  (read-asset [this options]
    (assoc this :content
           (with-open [in (java.io.BufferedInputStream. (java.io.FileInputStream. (:file this)))]
             (let [buf (make-array Byte/TYPE (.length (:file this)))]
               (.read in buf)
               buf))))
  Compressor
  (compress [this options]
    (:content this)))

(def types "mapping of file types to constructor functions"
  (atom {}))

(defn register [file-ext constructor-fn]
  "register a new asset constructor for files with the file-ext"
  (swap! types assoc file-ext constructor-fn))

(defn make-asset [filename]
  "returns a newly constructed asset of the proper type as determined by the file extension.
defaults to Static if extension is not registered."
  ((get @types (file-ext filename) map->Static) {:file filename}))

(register "js"  map->Js)
(register "css" map->Css)
