(ns dieter.jsengine
  (:require [dieter.settings :as settings]
            [dieter.pools :as pools]
            [dieter.rhino :as rhino]
            [dieter.v8 :as v8]))


;; TODO: take an asset to avoid slurping here
(defn run-compiler [pool preloads fn-name file & {:keys [engine]}]
  (try
    (let [input (slurp file)
          absolute (.getAbsolutePath file)
          filename (.getCanonicalPath file)
          args [input absolute filename]]
      (if (or (= (:engine settings/*settings*) :rhino)
              (= engine :rhino))
        (rhino/with-scope pool preloads
          (rhino/call fn-name args))
        (v8/with-scope pool preloads
          (v8/call fn-name args))))
    (catch Exception e
      (let [ste (StackTraceElement. "jsengine"
                                    "compileHamlCoffee" (.getPath file) -1)
            st (.getStackTrace e)
            new-st (into [ste ] st)
            new-st-array (into-array StackTraceElement new-st)]
        (.setStackTrace e new-st-array)
        (throw e)))))