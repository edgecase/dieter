(ns dieter.middleware.mime
  "Middleware for sending mime types of dieter files"
  (:require [ring.util.response :as response]))

(defn dieter-file-type [filename]
  (cond
   (re-matches #".*css-[\da-f]{32}\.dieter$" filename) "text/css"
   (re-matches #".*js-[\da-f]{32}\.dieter$" filename) "text/javascript"))

(defn wrap-dieter-mime-types
  [app]
  (fn [req]
    (let [{:keys [headers body] :as response} (app req)]
      (if (instance? java.io.File body)
        (let [filename (.getPath body)
              file-type (dieter-file-type filename)]
          (if file-type
            (-> response
                (response/content-type file-type))
            response))
        response))))
