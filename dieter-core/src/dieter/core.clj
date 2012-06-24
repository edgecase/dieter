(ns dieter.core
  (:require [clojure.java.io :as io]
            [fs])
  (:use
   dieter.settings
   dieter.asset
   [dieter.path :only [find-file cached-file-path make-relative-to-cache
                       uncachify-filename cache-busting-path write-file
                       relative-path]]
   [ring.middleware.file      :only [wrap-file]]
   [ring.middleware.file-info :only [wrap-file-info]]
   [dieter.middleware.expires :only [wrap-expires-never]]
   [dieter.asset.javascript   :only [map->Js]]
   [dieter.asset.css          :only [map->Css]]
   [dieter.asset.static       :only [map->Static]]
   [dieter.asset.less         :only [map->Less]]
   [dieter.asset.coffeescript :only [map->Coffee]]
   [dieter.asset.hamlcoffee   :only [map->HamlCoffee]]
   [dieter.asset.manifest     :only [map->Dieter]]))

(register :default map->Static)
(register "coffee" map->Coffee)
(register "cs"     map->Coffee)
(register "css"    map->Css)
(register "dieter" map->Dieter)
(register "hamlc"  map->HamlCoffee)
(register "js"     map->Js)
(register "less"   map->Less)

(defn write-to-cache [content requested-path]
  (let [dest (io/file (cached-file-path requested-path content))]
    (io/make-parents dest)
    (write-file content dest)
    dest))

(def known-mime-types
  {:hbs "text/javascript"
   "less" "text/css"
   "hamlc" "text/javascript"
   "coffee" "text/javascript"
   "cs" "text/javascript"})

(defn find-and-cache-asset [requested-path]
  (when-let [file (find-file requested-path (asset-root))]
    (-> file
        (make-asset)
        (read-asset *settings*)
        (compress *settings*)
        (write-to-cache requested-path))))

(defn asset-builder [app & [options]]
  (fn [req]
    (with-options options
      (let [path (uncachify-filename (:uri req))]
        (if (re-matches #"^/assets/.*" path)
          (if-let [cached (find-and-cache-asset (str "." path))]
            (let [new-path (make-relative-to-cache (str cached))]
              (add-cached-path path new-path)
              (app (assoc req :uri new-path)))
            (app req))
          (app req))))))

(defn foreach-file
  "Iterate through the assets directory"
  [dir f]
  (fs/walk
   dir
   (fn [root _ files]
     (doseq [file files]
       (f (->> file
               (fs/join root)))))))

(defn precompile [options]
  (with-options options
    (-> *settings* :cache-root (fs/join "assets") fs/deltree)
    (foreach-file
     (fs/join (asset-root) "assets")
     (fn [filename]
       (try (->> filename
                 (relative-path (asset-root))
                 (str "./")
                 (find-and-cache-asset))
            (print ".")
            (catch Exception e
              (println "Not built" filename)))))
    nil))


(defn asset-pipeline [app & [options]]
  (with-options options
    (if (= :production (:cache-mode *settings*))
      (-> app
          (wrap-file (cache-root))
          (asset-builder *settings*)
          (wrap-file (cache-root))
          (wrap-file-info known-mime-types)
          (wrap-expires-never))
      (-> app
          (wrap-file (cache-root))
          (asset-builder *settings*)
          (wrap-file-info known-mime-types)))))

(defn link-to-asset [path & [options]]
  "path should start under assets and not contain a leading slash
ex. (link-to-asset \"javascripts/app.js\") => \"/assets/javascripts/app-12345678901234567890123456789012.js\""
  (with-options options
    (if-let [file (find-file (str "./assets/" path) (asset-root))]
      (cache-busting-path *settings* (str "/assets/" path)))))

(defn load-precompiled-assets
  "Load any assets already in the cache directory"
  []
  (foreach-file
   (cache-root)
   (fn [cached]
     (let [cached (->> cached
                       (relative-path (cache-root))
                       (str "/"))
           uncached (->> cached
                         (uncachify-filename))]
       (add-cached-path uncached cached)))))

(defn init [options]
  (with-options options
    (load-precompiled-assets)))