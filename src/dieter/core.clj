(ns dieter.core
  (:require
   [clojure.string :as cstr]
   [clojure.java.io :as io])
  (:use
   dieter.settings
   [dieter.manifest :only [manifest-files]]
   [dieter.path :only [find-file cached-file-path file-ext make-relative-to-cache cache-busting-path]]
   [dieter.compressor :only [compress-js compress-css]]
   [dieter.preprocessors.handlebars :only [preprocess-handlebars]]
   [dieter.preprocessors.less :only [preprocess-less]]))

(comment "TODO:"
         "logging")

(defn write-to-cache [content requested-path]
  (let [dest (io/file (cached-file-path requested-path content))]
    (io/make-parents dest)
    (spit dest content)
    dest))

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

(defmethod preprocess-file :less [file]
  (preprocess-less file))

(defn compress [text requested-path]
  "optionally compress (minify) text, according to settings and file type"
  (if (:compress *settings*)
    (case (file-ext requested-path)
      "js" (compress-js text)
      "css" (compress-css text)
      text)
    text))

(defn find-and-cache-asset [requested-path]
  (if-let [file (find-file requested-path (asset-root))]
    (-> file
        (preprocess-file)
        (compress requested-path)
        (write-to-cache requested-path))))

(defn asset-pipeline [app & [options]]
  (fn [req]
    (binding [*settings* (merge *settings* options)]
      (let [path (:uri req)]
        (if (re-matches #"^/assets/.*" path)
          (if-let [cached (find-and-cache-asset (str "." path))]
            (let [new-path (make-relative-to-cache (str cached))]
              (swap! cached-paths assoc path new-path)
              (app (assoc req :uri new-path)))
            (app req))
          (app req))))))

(defn link-to-asset [path options]
  (binding [*settings* (merge *settings* options)]
    (if-let [file (find-file (str "./assets/" path) (asset-root))]
      (cache-busting-path *settings* (cstr/replace (.getCanonicalPath file) (absolute-asset-root) "")))))
