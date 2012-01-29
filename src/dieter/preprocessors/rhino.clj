(ns dieter.preprocessors.rhino
  (:require [clojure.java.io :as io])
  (:import [org.mozilla.javascript Context NativeObject]))

(defmacro with-context [context & body]
  `(let [~context (Context/enter)]
     (try ~@body
          (finally (Context/exit)))))

(defmacro defscope [name & preloads]
  `(with-context context#
     (let [scope#  (.initStandardObjects context#)]
       (doseq [file# '~preloads]
         (load-vendor file# context# scope#))
       (defonce ~name scope#))))

(defn call [fn-name scope & args]
  (with-context cx
    (let [fun (.get scope fn-name scope)]
      (.call fun cx scope nil (into-array args)))))

(defn load-vendor [filename context scope]
  (.evaluateReader context scope
                   (io/reader (io/resource (str "vendor/" filename)))
                   filename 1 nil))

(defn js-keys [obj]
  (seq (NativeObject/getPropertyIds obj)))