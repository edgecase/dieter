(ns dieter.test.precompile
  (:require [dieter.core :as core]
            [dieter.precompile :as precompile]
            [dieter.settings :as settings]
            [dieter.cache :as cache]
            [clojure.java.io :as io])
  (:use clojure.test
        ring.mock.request))

(deftest precompile-roundtrip-works
  (let [options {:engine :v8
                 :compress false
                 :log-level :quiet
                 :asset-root "test/fixtures"
                 :cache-root "test/precompile-cache"
                 :cache-mode :production
                 :precompiles ["javascripts/app.js"]}]
    (precompile/precompile options)
    (settings/with-options options
      (let [old @cache/cached-uris]
        (swap! cache/cached-uris (constantly {}))
        (precompile/load-precompiled-assets)
        (is (= old @cache/cached-uris))))))