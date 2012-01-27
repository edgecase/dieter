(ns dieter.preprocessors.rhino
  (:require [clojure.java.io :as io])
  (:import [org.mozilla.javascript Context NativeObject]))

(defmacro with-rhino [context scope & body]
  `(let [~context (Context/enter)
         ~scope (.initStandardObjects ~context)]
     (try ~@body
          (finally (Context/exit)))))

(defn load-vendor [filename context scope]
  (.evaluateReader context scope
                   (io/reader (io/resource (str "vendor/" filename)))
                   filename 1 nil))

(defn js-keys [obj]
  (seq (NativeObject/getPropertyIds obj)))