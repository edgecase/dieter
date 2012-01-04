(ns dieter.core
  (:require [clojure.string :as cstr]
            [clojure.java.io :as io]
            [ring.middleware.file :as ring])
  (:import [java.io File FileReader PushbackReader])
  )

(defn load-manifest [file]
  (let [stream (PushbackReader. (FileReader. file))]
    (read stream)))

(defn locate-file [filename basedir]
  (first (filter #(= (.getName %) filename) (file-seq basedir))))

(defn package-asset [manifest-file]
  (let [manifest (load-manifest manifest-file)]
    (cstr/join "\n"
               (map (fn [file]
                      (slurp (locate-file file (.getParentFile manifest-file))))
                    manifest))))

(defn cache-path [requested-file]
  (cstr/replace-first requested-file "/assets/" "/asset-cache/assets/"))

(defn log [& messages]
  (locking System/out (apply println messages)))

(defn write-to-cache [src requested-path]
  (let [dest (io/file (cache-path requested-path))]
    (io/make-parents dest)
    (if (string? src)
      (spit dest src)
      (io/copy (io/file requested-path) dest))))

(defn find-file [src-path]
  (let [asset (io/file src-path)]
    (if (and (.exists asset) (.isFile asset))
      asset
      nil)))

(defn find-manifest [src-path]
  (find-file (str src-path ".dieter")))

(defn find-and-cache-assets [requested-path]
  (if-let [file (find-file requested-path)]
    (write-to-cache file requested-path)
    (if-let [file (find-manifest requested-path)]
      (write-to-cache (package-asset file) requested-path))))

(defn asset-pipeline [app & options]
  (fn [req]
    (if (re-matches #"^/assets/.*" (:uri req))
      (find-and-cache-assets (str "./resources" (:uri req))))
    (app req)))
