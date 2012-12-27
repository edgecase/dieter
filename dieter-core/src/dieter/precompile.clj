(ns dieter.precompile
  (:require [fs])
  (:use [dieter.settings :as settings]
        [dieter.asset :as asset]
        [dieter.path :only [find-file  make-relative-to-cache
                       uncachify-filename cache-busting-path write-file
                       relative-path]]))

(defn foreach-file
  "Iterate through the assets directory"
  [dir f]
  (fs/walk
   dir
   (fn [root _ files]
     (doseq [file files]
       (f (->> file
               (fs/join root)))))))

(defn load-precompiled-assets
  "Load any assets already in the cache directory"
  []
  (foreach-file
   (settings/cache-root)
   (fn [cached]
     (let [cached (->> cached
                       (relative-path (cache-root))
                       (str "/"))
           uncached (->> cached
                         (uncachify-filename))]
       (add-cached-path uncached cached)))))

(defn find-and-cache-asset [& args]
  (apply (ns-resolve 'dieter.core 'find-and-cache-asset)))

(defn precompile [options] ;; lein dieter-precompile uses this name
  (settings/with-options options
    (-> settings/*settings* :cache-root (fs/join "assets") fs/deltree)
    (if (:precompiles settings/*settings*)
      (doseq [filename (:precompiles settings/*settings*)]
        (->> filename
             (str "./")
             (find-and-cache-asset)))
      (doseq [asset-root (settings/asset-roots)]
        (foreach-file
         (fs/join asset-root "assets")
         (fn [filename]
           (try (->> filename
                     (relative-path asset-root)
                     (str "./")
                     (find-and-cache-asset))
                (print ".")
                (catch Exception e
                  (println "Not built" filename)))))
        nil))))