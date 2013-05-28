(ns dieter.precompile
  (:require [clojure.java.io :as io]
            [dieter.cache :as cache]
            [dieter.path :as path]
            [dieter.asset :as asset]
            [dieter.settings :as settings]))

(defn relative-path [root file]
  (let [absroot (.getCanonicalPath (io/file root))
        absfile (.getCanonicalPath (io/file file))
        root-length (count absroot)]
    (.substring absfile (inc root-length))))

(defn load-precompiled-assets
  "Load any assets already in the cache directory"
  []
  (->> (settings/cache-root)
       (io/file)
       file-seq
       flatten
       (remove #(.isDirectory %))
       (map (fn [cached]
              (let [cached (->> cached
                                (relative-path (settings/cache-root))
                                (str "/"))
                    uncached (->> cached
                                  (path/uncachify-path))]
                (cache/add-cached-uri uncached cached))))
       dorun))

(defn find-and-cache-asset [& args]
  (apply (ns-resolve 'dieter.core 'find-and-cache-asset) args))

(defn delete-dir [directory]
  (->> directory
       io/file
       file-seq
       flatten
       (remove #(= directory (.getPath %1)))
       reverse
       (map #(.delete %1))
       dorun))

(defn precompile [options]
  (settings/with-options options
    (-> (settings/cache-root) (str "assets") delete-dir)
    (if (settings/precompiles)
      (doseq [filename (settings/precompiles)]
        (->> filename
             (find-and-cache-asset)))
      (doseq [asset-root (settings/asset-roots)]
        (->>
         (io/file asset-root "assets")
         file-seq
         flatten
         (remove #(.isDirectory %))
         (map (fn [filename]
                (try (->> filename
                     (relative-path asset-root)
                     (str "./")
                     (find-and-cache-asset))
                (print ".")
                (catch Exception e
                  (println "Not built" filename)))))
         dorun)))))