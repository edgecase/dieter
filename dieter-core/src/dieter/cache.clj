(ns dieter.cache
  (:require [clojure.string :as cstr]
            [dieter.settings :as settings]
            [dieter.path :as path]
            [clojure.java.io :as io])
  (:import [java.security MessageDigest]))

(derive (class (make-array Byte/TYPE 0)) ::bytes)
(derive java.lang.String ::string-like)
(derive java.lang.StringBuilder ::string-like)

(defmulti write-file (fn [c f] (class c)))
(defmethod write-file ::string-like [content file]
  (spit file content))

(defmethod write-file ::bytes [content file]
  (with-open [out (java.io.FileOutputStream. file)]
    (.write out content)))

(defmulti md5 class)
(defmethod md5 ::bytes [bytes]
  (let [digest (.digest (MessageDigest/getInstance "MD5") bytes)]
    (format "%032x" (BigInteger. 1 digest))))

(defmethod md5 ::string-like [string]
  (md5 (.getBytes (str string) "UTF-8")))

(defn add-md5 [path content]
  (if-let [[match fname ext] (re-matches #"^(.+)\.(\w+)$" path)]
    (str fname "-" (md5 content) "." ext)
    (str path "-" (md5 content))))

(defn cached-file-path
  "given the request path, generate the filename of where the file
will be cached. Cache is rooted at cache-root/assets/ so that
static file middleware can be rooted at cache-root"
  [requested-file content]
  (add-md5 (cstr/replace-first requested-file "/assets/"
                               (str "/" (settings/cache-root) "/assets/"))
           content))

(defn write-to-cache [content relpath]
  (let [dest (io/file (cached-file-path relpath content))]
    (io/make-parents dest)
    (write-file content dest)
    dest))


(defonce cached-uris (atom {}))

(defn add-cached-uri [uri new-uri]
  (swap! cached-uris assoc uri new-uri))

(defn cache-busting-uri [uri]
  "in production mode, append a md5 of the file contents to the file path"
  (if (settings/production?)
    (or (get @cached-uris uri)
        ;; if we dont have it, use the date in lieu
        (add-md5 uri (str (java.util.Date.))))
    uri)) ;; always reload