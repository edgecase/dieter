(ns dieter.v8
  (:require [v8.core :as v8]
            [clojure.java.io :as io]
            [fs]
            [clojure.string :as str]
            [dieter.settings :as settings]))

(defn load-vendor [files]
  (apply str (map (fn [f]
                    (->> f
                         (fs/join "vendor")
                         io/resource
                         io/reader
                         line-seq
                         (interpose "\n")
                         (concat "\n\n")
                         (apply str)))
                  files)))

(defn escape-value [val]
  (str/escape val {\\ "\\\\" \" "\\\"" \newline "\\n"}))

(defn construct-call [fn-name args]
  (let [eargs (map (fn [a] (escape-value a)) args)
        qargs (map (fn [e] (str \" e \")) eargs)
        iargs (interpose \, qargs)
        jargs (apply str iargs)]
    (str fn-name \( jargs \) \;)))

(def ^:dynamic context nil)

(defn create-context [preloads]
  (println "loading context")
  (let [cx (v8/create-context)]
    (v8/run-script-in-context cx (load-vendor preloads))
    cx))

(defmacro with-scope [pool preloads & body]
  `(try
     (pools/with-pool ~pool ~'pool-entry #(create-context ~preloads)
       (binding [context ~'pool-entry]
         ~@body))))

(defn call [fn-name args]
  (let [script (construct-call fn-name args)]
    (v8/run-script-in-context context script)))
