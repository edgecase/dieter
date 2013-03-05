(ns leiningen.dieter-precompile
  "Precompile dieter assets"
  (:require [clojure.string :as string]
            [leiningen.core.eval :as eval]))

(defn eval-opt [project opt]
  (eval/eval-in-project project `(var-get (resolve (symbol ~opt))) `(require (symbol (namespace (symbol ~opt))))))

(defn resolve-dieter-options [project opt]
  (cond
   (map? opt) opt
   (string? opt) (eval-opt project opt)))

(defn dieter-precompile
  [project]
  (let [option-param (:dieter-options project)
        options (resolve-dieter-options project option-param)]
    (println (nil? options))
    (println "options=" options)
    (eval/eval-in-project
     project
     `(let []
        (dieter.core/precompile ~options))
     `(require 'dieter.core))))