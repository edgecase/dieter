(ns dieter.sass
  (:use [clojure.java.shell :only [sh]]))

(defn process [file]
  (sh "/Users/pairfive/.rvm/bin/rvm-shell" "1.9.3@dieter" "-c" (str  "\"sass " (.getAbsolutePath file) "\""))
  )