(ns dieter.path
  (:use dieter.settings)
  (:require [clojure.string :as cstr]
            [clojure.java.io :as io]
            [dieter.settings :as settings]
            [fs])
  (:import [java.security MessageDigest]))

;;;; TODO
;;; so manifests need to call find-file from their own start-dir, not the
;;; asset-root. So this needs to be flexible to support both. However, it should
;;; never be splitting things apart, and "./" isn't neceessary (it should be
;;; handled in the manifest).

(defn file-ext [file]
  (last (cstr/split (str file) #"\.")))

(defn uncachify-path [path]
  (if-let [[match fname hash ext] (re-matches #"^(.+)-([\da-f]{32})\.(\w+)$" path)]
    (str fname "." ext)
    path))

(defn make-relative-to-cache [path]
  (cstr/replace-first path (re-pattern (str ".*" (settings/cache-root))) ""))


(defn relative-path [root file]
  (let [absroot (fs/abspath root)
        absfile (fs/abspath file)
        root-length (count absroot)]
    (.substring absfile (inc root-length))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; String-types used
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Dieter uses many different types of paths, and it can be confusing at times.
;;; We try to use a sort-of hungarian notation, where every path has a type
;;; included in its variable name somehow.

;;; A "path" refers to paths of any kind, including filenames, uris, adrfs, etc.

;;; An "Asset-directory-relative filename" (adrf) represents a path, relative
;;; to the asset directory. It is used as a canonical representation, and can
;;; easily by created from URIs and filenames from directory traversals. It can
;;; also easily be converted into any of these types of file. It does not
;;; include the string "/assets/".

;;; A URI represents the part of the URI that we use in dieter. If a whole URI
;;; is prototcol://hostname/path, then we use URI to represent just the "path"
;;; portion. In dieter, all URIs will start with "/assets/", as otherwise they
;;; won't be handled by dieter.

;;; A filename represents an actual file on the filesystem. We'll try and keep
;;; them as absolute strings names, because relative ones are easy to confuse
;;; with other types.

(defn is-asset-uri? [uri]
  (re-matches #"^/assets/.*" uri))

(defn uri->adrf [uri]
  {:pre [(is-asset-uri? uri)]} ;; uris start with "/assets"
  (.substring uri 8))

(defn adrf->uri [adrf]
  {:post [(is-asset-uri? %)]} ;; uris start with "/assets"
  (str "/assets/" adrf))

(defn adrf->filename [root adrf]
  (str root "/assets/" adrf))

(defn find-file [filename & {:keys [root]}]
  {:post [(or (nil? %) (.exists %))]}
  (let [file (io/file root filename)]
    (when (.exists file)
      file)))

(defn find-asset [adrf]
  {:post [(or (nil? %) (-> % io/file .exists))]}
  (reduce #(or %1 (find-file (adrf->filename %2 adrf)))
          nil
          (settings/asset-roots)))
