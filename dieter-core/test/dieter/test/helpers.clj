(ns dieter.test.helpers
  (:require [dieter.settings :as settings]))

(defn has-text?
  "returns true if expected occurs in text exactly n times (one or more times if not specified)"
  ([text expected]
     (not= -1 (.indexOf text expected)))
  ([text expected times]
     (= times (count (re-seq (re-pattern expected) text)))))

(defmacro with-both-engines [& body]
  `(binding [settings/*settings* (merge settings/*settings* {:engine :rhino})]
     ~@body)
  `(binding [settings/*settings* (merge settings/*settings* {:engine :v8})]
     ~@body))