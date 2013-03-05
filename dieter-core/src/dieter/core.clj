(ns dieter.core
  (:require [dieter.settings :as settings]
            [dieter.asset :as asset]
            [dieter.path :as path]
            [dieter.cache :as cache]
            [dieter.util]
            [dieter.precompile :as precompile]
            [dieter.asset.coffeescript]
            [dieter.asset.css]
            [dieter.asset.hamlcoffee]
            [dieter.asset.javascript]
            [dieter.asset.less]
            [dieter.asset.manifest]
            [dieter.asset.static])
  (:use [ring.middleware.file      :only [wrap-file]]
        [ring.middleware.file-info :only [wrap-file-info]]
        [dieter.middleware.expires :only [wrap-file-expires-never]]
        [dieter.middleware.mime    :only [wrap-dieter-mime-types]]))


(defn find-and-cache-asset [adrf]
  (when-let [file (path/find-asset adrf)]
    (-> file
        (asset/make-asset)
        (asset/read-asset)
        (#(if (settings/compress?)
            (asset/compress %)
            (:content %)))
        (cache/write-to-cache adrf))))

(defn asset-builder [app & [options]]
  (fn [req]
    (settings/with-options options
      (let [uri (-> req :uri)]
        (if (path/is-asset-uri? uri)
          (if-let [cached-filename (-> uri
                                       path/uri->adrf
                                       path/uncachify-path
                                       find-and-cache-asset)]
            (let [new-uri (path/make-relative-to-cache cached-filename)]
              (cache/add-cached-uri uri new-uri)
              (app (assoc req :uri new-uri)))
            (app req))
          (app req))))))

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Entry points
;;;;;;;;;;;;;;;;;;;;;;;;;

(def known-mime-types {:hbs "text/javascript"
                       "less" "text/css"
                       "hamlc" "text/javascript"
                       "coffee" "text/javascript"
                       "cs" "text/javascript"})

(defn asset-pipeline
  "Construct the Dieter asset pipeline depending on the :cache-mode option, eventually
   either loading the data from the cache directory, rendering a new resource and
   returning that, or passing on the request to the previously existing request
   handlers in the pipeline."
  [app & [options]]
  (settings/with-options options
    (if (settings/production?)
      (-> app
          (wrap-file (settings/cache-root))
          (asset-builder options)
          (wrap-file-expires-never (settings/cache-root))
          (wrap-file-info known-mime-types)
          (wrap-dieter-mime-types))
      (-> app
          (wrap-file (settings/cache-root))
          (asset-builder options)
          (wrap-file-info known-mime-types)
          (wrap-dieter-mime-types)
          (wrap-file-info known-mime-types)))))


(defn link-to-asset [adrf & [options]]
  "path should start under assets and not contain a leading slash
ex. (link-to-asset \"javascripts/app.js\") => \"/assets/javascripts/app-12345678901234567890123456789012.js\""
  (settings/with-options options
    (if (path/find-asset adrf)
      (-> adrf path/adrf->uri cache/cache-busting-uri))))


(defn precompile [options] ;; lein dieter-precompile uses this name
  (precompile/precompile options))


(defn init [options]
  (settings/with-options options
    (precompile/load-precompiled-assets)))
