(ns leiningen.dieter-precompile
  "Precompile dieter assets"
  (:require [clojure.string :as string]
            [leiningen.core.eval :as eval]))

(defn eval-opt [project opt result-file]
  (last [(eval/eval-in-project
   project
   `(with-open [w# (java.io.FileWriter. ~result-file)] (print-dup (var-get (resolve (symbol ~opt))) w#))
   `(require (symbol (namespace (symbol ~opt))))) :from-file]))

(defn resolve-dieter-options [project opt result-file]
  (cond
   (map? opt) opt
   (string? opt) (eval-opt project opt (.getAbsolutePath result-file))))

(defn dieter-precompile
  [project]
  (let [result-file (java.io.File/createTempFile "lein-dieter" "options")
        option-param (:dieter-options project)
        options-result (resolve-dieter-options project option-param result-file)
        options (if (= options-result :from-file) (read-string (slurp result-file)) options-result)]
    (println "options=" options)
    (eval/eval-in-project
     project
     `(let []
        (dieter.core/precompile ~options))
     `(require 'dieter.core))))