(ns dieter.jsengine
  (:require [dieter.settings :as settings]
            [dieter.pools :as pools]
            [dieter.rhino :as rhino]))


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
        (do
          (require 'v8.core) ;v8 is not always available, load it at the last possible moment
          (let [ws   (ns-resolve 'v8.core :with-scope)
                call (ns-resolve 'v8.core :call)]
            (ws pool preloads
              (call fn-name args))))))
    (catch Exception e
      (let [ste (StackTraceElement. "jsengine"
                                    "compileHamlCoffee" (.getPath file) -1)
            st (.getStackTrace e)
            new-st (into [ste ] st)
            new-st-array (into-array StackTraceElement new-st)]
        (.setStackTrace e new-st-array)
        (throw e)))))
