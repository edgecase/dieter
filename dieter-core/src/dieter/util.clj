(ns dieter.util
  (:require [clojure.java.io :as io]))

(defn slurp-into [#^StringBuilder builder f]
  "read file contents into an existing string builder"
  (with-open [#^java.io.Reader r (io/reader f)]
    (loop [c (.read r)]
      (if (neg? c)
        builder
        (do
          (.append builder (char c))
          (recur (.read r)))))))

(defn string-builder [& args]
  (let [builder (StringBuilder.)]
    (doseq [arg args]
      (.append builder arg))
    builder))
