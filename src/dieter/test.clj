(ns dieter.test
  (:use [dieter.core :only [asset-pipeline]]
        ring.adapter.jetty
        ring.handler.dump
        ring.middleware.stacktrace
        ring.middleware.file
        ring.middleware.file-info))

(defn four-oh-four [app]
  (fn [req]
    {:body "File Not Found"
     :status 404
     :headers {"Content-Type" "text/html"}}))

(def app
  ; This goes bottom to top, fyi
  (-> wrap-file-info
      (four-oh-four)
      (wrap-file "resources/asset-cache")
      (asset-pipeline {:compress true})
      (wrap-file "resources/asset-cache")
      wrap-stacktrace
      ))

(defn start []
  (run-jetty app {:port 8080}))

(defn -main [& m]
  (start))