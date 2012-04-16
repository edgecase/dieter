(ns leiningen.dieter-precompile-runtime
  "Precompile dieter assets"
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