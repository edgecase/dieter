(ns dieter.precompile
  (:require [fs]
            [dieter.cache :as cache]
            [dieter.path :as path]
            [dieter.asset :as asset]
            [dieter.settings :as settings]))

(defn foreach-file
  "Iterate through the assets directory"
  [dir f]
  (fs/walk
   dir
   (fn [root _ files]
     (doseq [filename files]
       (f (->> filename
               (fs/join root)))))))

(defn load-precompiled-assets
  "Load any assets already in the cache directory"
  []
  (foreach-file
   (settings/cache-root)
   (fn [cached]
     (let [cached (->> cached
                       (path/relative-path (settings/cache-root))
                       (str "/"))
           uncached (->> cached
                         (path/uncachify-path))]
       (cache/add-cached-uri uncached cached)))))

(defn find-and-cache-asset [& args]
  (apply (ns-resolve 'dieter.core 'find-and-cache-asset)))

(defn precompile [options] ;; lein dieter-precompile uses this name
  (settings/with-options options
    (-> (settings/cache-root) (fs/join "assets") fs/deltree)
    (if (settings/precompiles)
      (doseq [filename (settings/precompiles)]
        (->> filename
             (str "./")
             (find-and-cache-asset)))
      (doseq [asset-root (settings/asset-roots)]
        (foreach-file
         (fs/join asset-root "assets")
         (fn [filename]
           (try (->> filename
                     (path/relative-path asset-root)
                     (str "./")
                     (find-and-cache-asset))
                (print ".")
                (catch Exception e
                  (println "Not built" filename)))))
        nil))))