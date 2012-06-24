(ns dieter.middleware.expires
  "Middleware for overriding expiration headers for cached Dieter resources"
  (:require [ring.util.response :as res])
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
      (res/header "Expires"
                  (.format (make-http-format) (Date. (+ (.currentTimeMilis System)
                                                        (* 1000 1 365 24 60 60))))))))
