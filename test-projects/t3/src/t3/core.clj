(ns t3.core
  (:require
   [ring.middleware.file :as ringfile]
   [ring.adapter.jetty :as jetty]
   [dieter.core :as dieter]))

(defn handler [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (str "Hello World from Ring" (dieter/link-to-asset "scripts.js"))})

(defn boot []
  (dieter/init {:cache-mode :production})
  (jetty/run-jetty
   (-> (-> #'handler (ringfile/wrap-file "filez"))
       (dieter/asset-pipeline {:cache-mode :production}))
   {:port 8080}))
