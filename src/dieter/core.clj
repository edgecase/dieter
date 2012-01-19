(ns dieter.core
  (:require [clojure.string :as cstr]
            [clojure.java.io :as io]
            [dieter.compressor :as compressor])
  (:use [dieter.preprocessors.handlebars :only [preprocess-handlebars]])
  (:import [java.io File FileReader PushbackReader]))

(comment "TODO:"
         "less preprocessor"
         "cache busting"
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
  (cond
   (.isDirectory relative-file) (io/file start-dir relative-file)
   (.getParent relative-file) (io/file start-dir (.getParent relative-file))
   :else start-dir))

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

(defn distinct-by
  "Returns a lazy sequence of the elements of coll with duplicates removed.
Duplicates are found by comparing the results of the comparison fn."
  [fun coll]
    (let [step (fn step [xs seen]
                   (lazy-seq
                    ((fn [[f :as xs] seen]
                      (when-let [s (seq xs)]
                        (if (contains? seen (fun f))
                          (recur (rest s) seen)
                          (cons f (step (rest s) (conj seen (fun f)))))))
                     xs seen)))]
      (step coll #{})))

(defn manifest-files [manifest-file]
  (distinct-by #(.getCanonicalPath %)
               (filter #(and (not (nil? %))
                             (not (.isDirectory %)))
                       (flatten
                        (map (fn [filename]
                               (if (re-matches #".*/$" filename)
                                 (file-seq (search-dir (io/file filename) (.getParentFile manifest-file)))
                                 (find-file filename (.getParentFile manifest-file))))
                             (load-manifest manifest-file))))))

(defmulti preprocess-file (fn [file] (keyword (file-ext file))))

(defmethod preprocess-file :default [file]
  (str "/* Source: " file " */\n" (slurp file)))

(defmethod preprocess-file :dieter [file]
  (cstr/join "\n" (map preprocess-file (manifest-files file))))

(defmethod preprocess-file :hbs [file]
  (preprocess-handlebars file))

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
