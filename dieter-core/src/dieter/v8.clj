(ns dieter.v8
  (:require [v8.core :as v8engine]
            [clojure.string :as str]
            [dieter.settings :as settings]))


(def ^:dynamic scope nil)

(defmacro with-context [& body]
  body)

(defmacro with-scope [pool preloads & body]
  `(binding [scope (load-vendor ~preloads)]
     ~@body
     ))

(defn escape-value [val]
  (str/escape val {\\ "\\\\" \" "\\\"" \newline "\\n"}))

(defn construct-call [fn-name args]
  (let [eargs (map (fn [a] (escape-value a)) args)
        qargs (map (fn [e] (str \" e \")) eargs)
        iargs (interpose \, qargs)
        jargs (apply str iargs)]
    (str fn-name \( jargs \) \;)))

(defn load-vendor [files]
  (apply str (map (fn [f] (str (slurp (str (settings/asset-root) "/vendor/" f)) "\n")) files)))


(defn call [fn-name & args]
  (let [script (str scope (construct-call fn-name args))]
    (v8engine/run-script script)))
