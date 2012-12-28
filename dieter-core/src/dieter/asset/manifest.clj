(ns dieter.asset.manifest
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [dieter.asset :as asset]
            [dieter.path :as path])
  (:use [dieter.util :only [slurp-into string-builder]])
  (:import [java.io FileReader PushbackReader FileNotFoundException]))

(defn load-manifest
  "a manifest file must be a valid clojure data structure,
namely a vector or list of file names or directory paths."
  [file]
  (let [stream (PushbackReader. (FileReader. file))]
    (read stream)))

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
  (->> (load-manifest manifest-file)
       (map (fn [filename]
              (let [file (if (re-matches #".*/$" filename)
                           (file-seq (path/search-dir filename (.getParentFile manifest-file)))
                           (path/find-file filename (.getParentFile manifest-file)))]

                (or file (throw (FileNotFoundException. (str "Cannot find " filename " from " manifest-file)))))))
       flatten
       (remove #(or (nil? %)
                    (.isDirectory %)
                    (re-matches #".*\.swp$" (.getCanonicalPath %))
                    (re-matches #"/.*\.#.*$" (.getCanonicalPath %))))
       (distinct-by #(.getCanonicalPath %))))

(defrecord Dieter [file]
  dieter.asset.Asset
  (read-asset [this]
    (let [builder (string-builder)
          target-name (s/replace (:file this) #".dieter$" "")
          result (asset/make-asset (io/file target-name))]
      (doseq [file (manifest-files (:file this))]
        (.append builder (:content (asset/read-asset (asset/make-asset file)))))
      (assoc result :content builder))))

(asset/register "dieter" map->Dieter)