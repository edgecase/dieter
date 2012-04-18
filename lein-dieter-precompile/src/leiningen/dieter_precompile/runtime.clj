(ns leiningen.dieter-precompile.runtime
  ;; dont use leiningen as the namespace, so that it doesn't appear in the
  ;; "help" section.
  "Precompile dieter assets, run-time portion"
  (:require [clojure.string :as string]
            [dieter.core]))

(defn from-ns [ns-string]
  (let [pair (string/split ns-string #"/")
        pair (map symbol pair)
        ns (first pair)
        sym (second pair)]
    (require ns)
    @(ns-resolve ns sym)))

(defn resolve-dieter-options [opt]
  (cond
   (map? opt) opt
   (string? opt) (let [val (from-ns opt)]
                   (cond
                    (map? val) val
                    (fn? val) (val)))))

(defn precompile [project]
  (let [options (->> project
                  :dieter-options
                  resolve-dieter-options)]
    (dieter.core/precompile options)
    nil))