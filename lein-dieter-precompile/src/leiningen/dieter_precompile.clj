(ns leiningen.dieter-precompile
  "Precompile dieter assets"
  (:require [clojure.string :as string]
            [leiningen.core.eval :as eval]))

(defn ns-resolve-string
  "Given a string "
  [ns-string]
  `(let [pair# (map symbol (string/split ~ns-string #"/"))
         ns# (first pair#)
         sym# (second pair#)]
     (require ns#)
     @(ns-resolve ns# sym#)))

(defn resolve-dieter-options [opt]
  `(let [opt# ~opt]
     (cond
      (map? opt#) opt#
      (string? opt#) (let [val# ~(ns-resolve-string opt)]
                       (cond
                        (map? val#) val#
                        (fn? val#) (val#))))))

(defn dieter-precompile
  [project]
  (let [options (:dieter-options project)
        options (->> project
                     :dieter-options
                     resolve-dieter-options)]
    (println "options=" options)
    (eval/eval-in-project
     project
     `(let []
        (dieter.core/precompile ~options))
     `(require 'dieter.core))))