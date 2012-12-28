(ns dieter.path
  (:use dieter.settings)
  (:require [clojure.string :as cstr]
            [clojure.java.io :as io]
            [dieter.settings :as settings]
            [fs])
  (:import [java.security MessageDigest]))

;;; Paths are either files or uris. Files are relative or absolute, but always
;;; represent a place in the filesystem. Uris are generally relative to the root
;;; of the domain. Relative uris and relative files can be converted, but care
;;; should be taken to not treat one as the other, as that's where errors happen.

(defn search-dir
  "return the directory to use as the root of a search for relative-file"
  [relative-path start-dir]
  (let [relative-file (io/file relative-path)
        relative-parent (.getParent relative-file)]
    (cond
     (re-matches #".*/$" relative-path) (io/file start-dir relative-path)
     relative-parent (io/file start-dir relative-parent)
     :else (io/file start-dir))))

(defn find-in-files [filename files]
  (let [[_ basename] (re-matches #"(^.*?)(?:\.\w+)?$" filename)
        pattern (re-pattern (str "^" basename ".*$"))]
    (or (first (filter #(= filename (.getName %)) files))
        (first (filter #(re-matches pattern (.getName %)) files)))))

(defn find-file [partial-path start-dir]
  (let [relative-file (io/file partial-path)
        filename (.getName relative-file)
        search-dir (search-dir partial-path start-dir)]
    (if (re-matches #"^\./.*" partial-path)
      (find-in-files filename (.listFiles search-dir))
      (find-in-files filename (file-seq search-dir)))))

(defn file-ext [file]
  (last (cstr/split (str file) #"\.")))

(defn uncachify-filename [filename]
  (if-let [[match fname hash ext] (re-matches #"^(.+)-([\da-f]{32})\.(\w+)$" filename)]
    (str fname "." ext)
    filename))



(defn make-relative-to-cache [path]
  (cstr/replace-first path (re-pattern (str ".*" (settings/cache-root))) ""))


(defn relative-path [root file]
  (let [absroot (fs/abspath root)
        absfile (fs/abspath file)
        root-length (count absroot)]
    (.substring absfile (inc root-length))))


;;;;;;;;;;;;;;;;;
;;; Uri
;;;;;;;;;;;;;;;;;

(defn is-asset-uri? [uri]
  (re-matches #"^/assets/.*" uri))

(defn uri->arf [uri]
  ;; uris start with "/"
  (str "." uri))

(defn uncachify-uri [uri] ;; same implementation
  (uncachify-filename uri))