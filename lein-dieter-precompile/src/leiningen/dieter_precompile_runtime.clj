(ns leiningen.dieter-precompile-runtime
  "Precompile dieter assets"
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [fs]
            [dieter.settings]
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

(defn relative-path [root file]
  (let [absroot (fs/abspath root)
        absfile (fs/abspath file)
        root-length (count absroot)]
    (.substring absfile (inc root-length))))

(defn precompile [project]
  (let [opts (->> project
                  :dieter-options
                  resolve-dieter-options
                  (merge dieter.settings/*settings*))
        asset-root (:asset-root opts)]

    (fs/walk
     (fs/join asset-root "assets")
     (fn [root dirs files]
       (doseq [file files]
         (try
           (binding [dieter.settings/*settings* opts]
             (->> file
                  (fs/join root)
                  (relative-path asset-root)
                  (str "./")
                  (dieter.core/find-and-cache-asset))
             (println "Built" (fs/join root file)))
           (catch Exception e
             (println "Not built" (fs/join root file)))))))
    nil))