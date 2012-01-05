(ns dieter.core
  (:require [clojure.string :as cstr]
            [clojure.java.io :as io])
  (:import [java.io File FileReader PushbackReader]))

(comment "TODO:"
  "if a manifest entry starts with ./ it is relative"
  "manifest entries ending with / require tree"
  "manifest entries can specify part of the path"
  "manifest entries without ./ have a lookup order. (current-dir asset-root vendor-root jar-root?)"
  "files should only ever be included once, the first time they are encountered"
  "js minification"
  "css minification"
  "handlebars preprocessor"
  "sass preprocessor"
  "include comment about original source of file"
  "conditionally compress"
  )

(def ^:dynamic *settings*
  {:compress false
   :require-paths ["resources/assets"]})

(defn load-manifest [file]
  (let [stream (PushbackReader. (FileReader. file))]
    (read stream)))

(defn cache-path [requested-file]
  (cstr/replace-first requested-file "/assets/" "/resources/asset-cache/assets/"))

(defn write-to-cache [string requested-path]
  (let [dest (io/file (cache-path requested-path))]
    (io/make-parents dest)
    (println (str "writing to " dest))
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
  (println "find file " partial-path start-dir)
  (let [relative-file (io/file partial-path)
        filename (.getName relative-file)
        search-dir (search-dir relative-file start-dir)]
    (if (re-matches #"^\./.*" partial-path)
      (find-in-dir filename search-dir)
      (find-in-tree filename search-dir))))

(defn file-ext [file]
  (last (cstr/split (.getName file) #"\.")))


(defn preprocess-contents [file]
  (slurp file))

(declare preprocess-file)

(defn preprocess-dieter [manifest-file]
  (let [ manifest (load-manifest manifest-file)]
    (map (fn [filename]
           (preprocess-file (find-file filename (.getParentFile manifest-file))))
         manifest)))

(def file-type-dispatch
  {"js" preprocess-contents
   "dieter" preprocess-dieter
   "css" preprocess-contents
   })

(defn preprocess-file [file]
  (println "preprocessing file " file )
  (let [type (file-ext file)
        preprocessor (file-type-dispatch type)]
    (preprocessor file)))

(defn find-and-cache-assets [requested-path]
  (if-let [file (find-file requested-path (io/file "resources/"))]
    (write-to-cache (preprocess-file file) requested-path)))

(defn asset-pipeline [app & [options]]
  (binding [*settings* (merge *settings* options)]
    (fn [req]
      (if (re-matches #"^/assets/.*" (:uri req))
        (find-and-cache-assets (str "." (:uri req))))
      (app req))))
