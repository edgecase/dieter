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
  (-> wrap-file-info
      (wrap-file "resources/asset-cache")
      (asset-pipeline)
      (wrap-file "resources/asset-cache")
      wrap-stacktrace
      (four-oh-four)
      ))

(defn start []
  (run-jetty app {:port 8080}))

(defn -main [& m]
  (println "wtf")
  (start))