(ns dieter.core
  (:require [clojure.string :as cstr]
            [clojure.java.io :as io]
            [dieter.compressor :as compressor])
  (:import [java.io File FileReader PushbackReader]))

(comment "TODO:"
  "manifest entries ending with / require tree"
  "manifest entries without ./ have a lookup order. (current-dir asset-root vendor-root jar-root?)"
  "files should only ever be included once, the first time they are encountered"
  "js minification"
  "css minification"
  "handlebars preprocessor"
  "sass preprocessor"
  "include comment about original source of file"
  "conditionally compress"
  )

(def ^:dynamic *settings* {:compress false
                           :require-paths ["resources/assets"]})

(defn load-manifest [file]
  (let [stream (PushbackReader. (FileReader. file))]
    (read stream)))

(defn cache-path [requested-file]
  (cstr/replace-first requested-file "/assets/" "/resources/asset-cache/assets/"))

(defn write-to-cache [string requested-path]
  (let [dest (io/file (cache-path requested-path))]
    (io/make-parents dest)
    (spit dest string)))

(defn search-dir [relative-file start-dir]
  (if (.getParent relative-file)
    (io/file start-dir (.getParent relative-file))
    start-dir))

(defn matches-filename? [filename file]
  (re-matches (re-pattern (str "^" filename ".*$")) (.getName file)))

(defn find-in-dir [filename dir]
  (first (filter (partial matches-filename? filename) (.listFiles dir))))

(defn find-in-tree [filename dir]
  (first (filter (partial matches-filename? filename) (file-seq dir))))

(defn find-file [partial-path start-dir]
  (let [relative-file (io/file partial-path)
        filename (.getName relative-file)
        search-dir (search-dir relative-file start-dir)]
    (if (re-matches #"^\./.*" partial-path)
      (find-in-dir filename search-dir)
      (find-in-tree filename search-dir))))

(defn file-ext [file]
  (last (cstr/split (str file) #"\.")))

(defn preprocess-contents [file]
  (str "// Source: " file "\n" (slurp file)))

(declare preprocess-file)

(defn preprocess-dieter [manifest-file]
  (cstr/join "\n" (map (fn [filename]
                    (preprocess-file (find-file filename (.getParentFile manifest-file))))
                  (load-manifest manifest-file))))

(def file-type-dispatch
  {"js" preprocess-contents
   "dieter" preprocess-dieter
   "css" preprocess-contents
   })

(defn preprocess-file [file]
  (let [type (file-ext file)
        preprocessor (file-type-dispatch type)]
    (preprocessor file)))

(defn compress [text requested-path]
  (if (:compress *settings*)
    (case (file-ext requested-path)
      "js" (compressor/compress-js text)
      "css" (compressor/compress-css text)
      text)
    text))

(defn find-and-cache-assets [requested-path]
  (if-let [file (find-file requested-path (io/file "resources/"))]
    (-> file
        (preprocess-file)
        (compress requested-path)
        (write-to-cache requested-path))))

(defn asset-pipeline [app & [options]]
  (fn [req]
    (binding [*settings* (merge *settings* options)]
      (if (re-matches #"^/assets/.*" (:uri req))
        (find-and-cache-assets (str "." (:uri req))))
      (app req))))
