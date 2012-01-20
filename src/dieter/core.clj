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
                           :asset-root "resources"
                           :cache-root "resources/asset-cache"})

(defn asset-root []
  (:asset-root *settings*))

(defn absolute-asset-root []
  (.getCanonicalPath (io/file (asset-root))))

(defn cache-root []
  (:cache-root *settings*))

(defn load-manifest
  "a manifest file must be a valid clojure data structure,
namely a vector or list of file names or directory paths."
  [file]
  (let [stream (PushbackReader. (FileReader. file))]
    (read stream)))

(defn cached-file-path
  "given the request path, generate the filename of where the file
will be cached. Cache is rooted at cache-root/assets/ so that
static file middleware can be rooted at cache-root"
  [requested-file]
  (cstr/replace-first requested-file "/assets/" (str "/" (cache-root) "/assets/")))

(defn write-to-cache [string requested-path]
  (let [dest (io/file (cached-file-path requested-path))]
    (io/make-parents dest)
    (spit dest string)
    dest))

(defn search-dir
  "return the directory to use as the root of a search for relative-file"
  [relative-file start-dir]
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
Duplicates are found by comparing the results of the comparison fn.
Implementation stolen from clojure.core/distinct"
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

(defn manifest-files
  "return a sequence of files specified by the given manifest.
Duplicates are included only once, the first time they are referenced.
Files not found are not returned and no error is indicated.
We should probably consider outputting some kind of warning in that case."
  [manifest-file]
  (distinct-by #(.getCanonicalPath %)
               (filter #(and (not (nil? %))
                             (not (.isDirectory %)))
                       (flatten
                        (map (fn [filename]
                               (if (re-matches #".*/$" filename)
                                 (file-seq (search-dir (io/file filename) (.getParentFile manifest-file)))
                                 (find-file filename (.getParentFile manifest-file))))
                             (load-manifest manifest-file))))))

(defmulti preprocess-file
  "handle different file types by optionally compiling them.
This is the main extension point for adding more precompilation types."
  (fn [file] (keyword (file-ext file))))

(defmethod preprocess-file :default [file]
  (str "/* Source: " file " */\n" (slurp file)))

(defmethod preprocess-file :dieter [file]
  (cstr/join "\n" (map preprocess-file (manifest-files file))))

(defmethod preprocess-file :hbs [file]
  (preprocess-handlebars file))

(defn compress [text requested-path]
  "optionally compress (minify) text, according to settings and file type"
  (if (:compress *settings*)
    (case (file-ext requested-path)
      "js" (compressor/compress-js text)
      "css" (compressor/compress-css text)
      text)
    text))

(defn find-and-cache-asset [requested-path]
  (if-let [file (find-file requested-path (asset-root))]
    (-> file
        (preprocess-file)
        (compress requested-path)
        (write-to-cache requested-path))))

(defn asset-pipeline [app & [options]]
  "return a middleware function "
  (fn [req]
    (binding [*settings* (merge *settings* options)]
      (if (re-matches #"^/assets/.*" (:uri req))
        (find-and-cache-asset (str "." (:uri req))))
      (app req))))

(defn link-to-asset
  "Return a link to the desired asset.
Has the side-effect of generating and cacheing the asset"
  ([path] (link-to-asset path {}))

  ([path options]
     (binding [*settings* (merge *settings* options)]
       (let [file (find-and-cache-asset (str "./assets/" path))]
         (cstr/replace (.getCanonicalPath file) (absolute-asset-root) "")))))
