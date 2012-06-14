(ns dieter.asset
  (:use [dieter.path :only [file-ext]]))

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

(def types "mapping of file types to constructor functions"
  (atom {}))

(defn register [file-ext constructor-fn]
  "register a new asset constructor for files with the file-ext"
  (swap! types assoc file-ext constructor-fn))

(defn make-asset [filename]
  "returns a newly constructed asset of the proper type as determined by the file extension.
defaults to Static if extension is not registered."
  ((get @types (file-ext filename) (:default @types)) {:file filename}))
