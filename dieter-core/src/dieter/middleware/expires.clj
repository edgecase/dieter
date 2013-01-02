(ns dieter.middleware.expires
  "Middleware for overriding expiration headers for cached Dieter resources"
  (:require [ring.util.response :as res]
            [ring.middleware.file :as file]
            [dieter.path :as path])
  (:import (java.util Date Locale TimeZone)
           java.text.SimpleDateFormat))

(defn ^SimpleDateFormat make-http-format
  "Formats or parses dates into HTTP date format (RFC 822/1123)."
  []
  (doto (SimpleDateFormat. "EEE, dd MMM yyyy HH:mm:ss ZZZ" Locale/US)
    (.setTimeZone (TimeZone/getTimeZone "UTC"))))

(defn wrap-expires-never
  "Middleware that overrides any existing headers for browser-side caching.
   This is meant to be used with Dieter's cached resources, since it sets
   the expiration headers to one year in the future (maximum suggested as
   per RFC 2616, Sec. 14.21)."
  [handler & [opts]]
  (fn [req]
    (if-let [resp (handler req)]
      (res/header resp "Expires"
                  (.format (make-http-format) (Date. (+ (System/currentTimeMillis)
                                                        (* 1000 1 365 24 60 60))))))))


(defn wrap-file-expires-never
  "Given a root path to the previously cached Dieter resources, check that the request
   points to something that is a cacheable URL for a precompiled resource. If so, pass
   the request to the ring file middleware and then set the expiration header, otherwise
   just pass the request along to the handlers down the chain."

  [app root-path & [opts]]
  (fn [req]
    (let [path (:uri req)
          uncached (path/uncachify-path path)]
      (if (and (re-matches #"^/assets/.*" path)
               (not (= path uncached)))
        (if-let [resp ((file/wrap-file app root-path) req)]
          (res/header resp "Expires"
                      (.format (make-http-format) (Date. (+ (System/currentTimeMillis)
                                                            (* 1000 1 365 24 60 60))))))
        (app req)))))
