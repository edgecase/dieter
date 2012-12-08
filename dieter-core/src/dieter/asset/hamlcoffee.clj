(ns dieter.asset.hamlcoffee
  (:require [dieter.pools :as pools]
            [clojure.string :as cstr]
            [dieter.asset]
            [dieter.asset.javascript]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce])
  (:use [dieter.jsengine :only (run-compiler)]))

(def pool (pools/make-pool))

(def memoized (atom {}))

(defn memoize-file [file f]
  (let [filename (.getCanonicalPath file)
        val (get @memoized filename)
        current-timestamp (-> file .lastModified time-coerce/from-long)
        saved-timestamp (:timestamp val)
        saved-content (:content val)]
    (if (and saved-content
             (time/before? current-timestamp saved-timestamp))

      ;; return already memory
      saved-content

      ;; compute new value and save it
      (let [new-content (f)]
        (dosync
         (swap! memoized assoc filename {:content new-content
                                         :timestamp (time/now)}))
        new-content))))


(defn preprocess-hamlcoffee [file]
  (memoize-file file
                #(run-compiler pool
                               ["coffee-script.js"
                                ;; imported direct from https://raw.github.com/netzpirat/haml-coffee/master/dist/compiler/hamlcoffee.js
                                "hamlcoffee.js"
                                "haml_coffee_assets-rhino-fix.js"
                                ;; imported direct from https://raw.github.com/netzpirat/haml_coffee_assets/master/lib/js/haml_coffee_assets.js
                                "haml_coffee_assets.js"
                                "hamlcoffee-wrapper.js"]
                               "compileHamlCoffee"
                               file)))

(defrecord HamlCoffee [file]
  dieter.asset.Asset
  (read-asset [this options]
    (dieter.asset.javascript.Js. (:file this) (preprocess-hamlcoffee (:file this)))))
