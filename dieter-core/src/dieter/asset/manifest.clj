(ns dieter.asset.manifest
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [dieter.asset :as asset]
            [dieter.path :as path]
            [fs])
  (:use [dieter.util :only [slurp-into string-builder]])
  (:import [java.io FileReader PushbackReader FileNotFoundException]))

(defn load-manifest
  "a manifest file must be a valid clojure data structure,
namely a vector or list of file names or directory paths."
  [file]
  (let [stream (PushbackReader. (FileReader. file))]
    (read stream)))

(defn recursive-files [dir]
  (->> dir
       fs/iterdir
       (map (fn [[root _ files]]
              (doall (map #(fs/join root %)
                          (sort files))))))) ;; sort because of file-ordering bugs

(defn manifest-files
  "return a sequence of files specified by the given manifest."
  [manifest-file]
  (->> (load-manifest manifest-file)
       (map (fn [filename]
              (let [dir (.getParent manifest-file)
                    file (path/find-file filename :root dir)]
                (when (nil? file)
                  (throw (FileNotFoundException. (str "Could not find " filename " from " manifest-file))))
                (if (-> file io/file .isDirectory)
                  (recursive-files file)
                  file))))
       doall
       flatten
       (map io/file)
       (remove #(or (re-matches #".*\.swp$" (.getCanonicalPath %))
                    (re-matches #"/.*\.#.*$" (.getCanonicalPath %))))))

(defn compile-manifest [file]
  (let [builder (string-builder)
        target-name (s/replace file #".dieter$" "")
        result (asset/make-asset (io/file target-name))]
    (doseq [mf (manifest-files file)]
      (->> mf
           asset/make-asset
           asset/read-asset
           :content
           (.append builder)))
    (assoc result :content builder)))

(defrecord Dieter [file]
  dieter.asset.Asset
  (read-asset [this] (compile-manifest (:file this))))

(asset/register "dieter" map->Dieter)