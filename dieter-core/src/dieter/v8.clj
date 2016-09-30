(ns dieter.v8
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [dieter.settings :as settings]))

(defn load-vendor [files]
  (apply str (map (fn [f]
                    (->> f
                         (str "vendor/")
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

;; Working around namespaces that fail to load is tricky; after much experimentation, this seems to be the best
;; approach: creating placeholds then overwriting them if the v8.core namespace actually loads.

(defn- fail [& _]
  (throw (IllegalStateException. "Use of v8 requires Mac OS X or Linux, plus special setup for the native v8 libraries.")))

(def v8-run-script-in-context fail)
(def v8-create-context fail)

(try
  ;; Trying to use a namespace alias, via :as, has just not worked, even when the catch block is triggered.
  (require 'v8.core)

  (def ^:private v8ns  (-> 'v8.core create-ns ns-map))
  
  ;; Redefine in terms of the actual functions. The Clojure compiler gets confused if we reference an aliased
  ;; namespace that didn't load so we get the namespace's map and extract the desired values directly.
  (def v8-run-script-in-context ('run-script-in-context v8ns))
  (def v8-create-context ('create-context v8ns))

  (catch Throwable e
    (.println (System/err) (str "Unable to load v8.core: " (.getMessage e) " (you may ignore if not using v8)"))))

(defn create-context [preloads]
  (let [cx (v8-create-context)]
    (v8-run-script-in-context cx (load-vendor preloads))
    cx))

(defmacro with-scope [pool preloads & body]
  `(try
     (pools/with-pool ~pool ~'pool-entry #(create-context ~preloads)
       (binding [context ~'pool-entry]
         ~@body))))

(defn call [fn-name args]
  (let [script (construct-call fn-name args)]
    (v8-run-script-in-context context script)))
