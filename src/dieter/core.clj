(ns dieter.core
  (:require
   dieter.asset.javascript
   dieter.asset.css
   dieter.asset.static
   dieter.asset.less
   dieter.asset.coffeescript
   dieter.asset.hamlcoffee
   dieter.asset.manifest
   [clojure.string :as cstr]
   [clojure.java.io :as io])
  (:use
   dieter.asset
   dieter.settings
   [dieter.path :only [find-file cached-file-path file-ext make-relative-to-cache
                       uncachify-filename cache-busting-path write-file]]
   [ring.middleware.file :only [wrap-file]]
   [ring.middleware.file-info :only [wrap-file-info]]))

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
  (if-let [file (find-file requested-path (asset-root))]
    (-> file
        (make-asset)
        (read-asset *settings*)
        (compress *settings*)
        (write-to-cache requested-path))))

(defn asset-builder [app & [options]]
  (fn [req]
    (binding [*settings* (merge *settings* options)]
      (let [path (uncachify-filename (:uri req))]
        (if (re-matches #"^/assets/.*" path)
          (if-let [cached (find-and-cache-asset (str "." path))]
            (let [new-path (make-relative-to-cache (str cached))]
              (swap! cached-paths assoc path new-path)
              (app (assoc req :uri new-path)))
            (app req))
          (app req))))))

(defn asset-pipeline [app & [options]]
  (binding [*settings* (merge *settings* options)]
    (if (= :production (:cache-mode *settings*))
      (-> app
          (wrap-file (cache-root))
          (asset-builder options)
          (wrap-file (cache-root))
          (wrap-file-info known-mime-types))
      (-> app
          (wrap-file (cache-root))
          (asset-builder options)
          (wrap-file-info known-mime-types)))))

(defn link-to-asset [path & [options]]
  "path should start under assets and not contain a leading slash
ex. (link-to-asset \"javascripts/app.js\") => \"/assets/javascripts/app-12345678901234567890123456789012.js\""
  (binding [*settings* (merge *settings* options)]
    (if-let [file (find-file (str "./assets/" path) (asset-root))]
      (cache-busting-path *settings* (str "/assets/" path)))))
