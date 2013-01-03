(ns dieter.test.core
  (:require [dieter.core :as core]
            [dieter.settings :as settings]
            [dieter.cache :as cache]
            [dieter.test.helpers :as h]
            [clojure.java.io :as io])
  (:use clojure.test))

(deftest test-link-to-asset
  (testing "development mode"
    (let [opts {:cache-mode :development :asset-root "test/fixtures" :cache-root "test/fixtures/asset-cache"}]
      (is (nil? (core/link-to-asset "javascripts/dontfindme.js" opts)))
      (is (= "/assets/javascripts/app.js" (core/link-to-asset "javascripts/app.js" opts)))
      (is (= "/assets/javascripts/manifest.js" (core/link-to-asset "javascripts/manifest.js" opts)))))

  (testing "production mode"
    (let [opts {:cache-mode :production
                :asset-root "test/fixtures"
                :cache-root "test/fixtures/asset-cache"}]

      (testing "no previous file generated"
        (is (re-matches #"/assets/javascripts/app-[\da-f]{32}\.js"
                        (core/link-to-asset "javascripts/app.js" opts))))

      (testing "file previously generated"
        (swap! cache/cached-uris assoc "/assets/javascripts/app.js"
               "/assets/javascripts/app-12345678901234567890af1234567890.js")

        (is (= "/assets/javascripts/app-12345678901234567890af1234567890.js"
               (core/link-to-asset "javascripts/app.js" opts)))))))


(deftest test-core-link-to-asset-in-secondary-dir
  (testing "development mode"
    (let [opts {:cache-mode :development
                :asset-roots ["test/fixtures" "test/fixtures/more_assets"]
                :cache-root "test/fixtures/asset-cache"}]
      (is (nil? (core/link-to-asset "javascripts/dontfindme.js" opts)))
      (is (= "/assets/javascripts/app.js"
             (core/link-to-asset "javascripts/app.js" opts)))
      (is (= "/assets/images/Elsa.jpg"
             (core/link-to-asset "images/Elsa.jpg" opts)))
      (is (= "/assets/javascripts/manifest.js.dieter"
             (core/link-to-asset "javascripts/manifest.js.dieter" opts))))))

(deftest test-write-to-cache
  (settings/with-options  {:asset-root "test/fixtures"
                           :cache-root "test/fixtures/asset-cache"}
    (let [content "var aString = 'of javascript';"
          adrf "javascripts/awesomesauce.js"
          cache-path (cache/write-to-cache content adrf)]
      (is (= "test/fixtures/asset-cache/assets/javascripts/awesomesauce-8be397d9c4a3c4ad35f33963fedad96b.js" (str cache-path)))
      (is (= content (slurp cache-path)))
      (.delete cache-path))))

(deftest test-asset-builder
  (settings/with-options {:asset-root "test/fixtures"
                          :cache-root "test/fixtures/asset-cache"}
    (let [app (fn [req] (:uri req))
          builder (core/asset-builder app)]
      (testing "plain file paths"
        (reset! cache/cached-uris {})
        (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js"
               (builder {:uri "/assets/javascripts/app.js"})))
        (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js"
               (get @cache/cached-uris "/assets/javascripts/app.js")))
        (.delete (io/file "test/fixtures/asset-cache/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js")))

      (testing "md5'd file paths"
        (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js"
               (builder {:uri "/assets/javascripts/app-12345678901234567890123456789012.js"})))
        (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js"
               (get @cache/cached-uris "/assets/javascripts/app.js")))
        (.delete (io/file "test/fixtures/asset-cache/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js")))

      (testing "binary files"
        (is (= "/assets/images/dieter-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg"
               (builder {:uri "/assets/images/dieter.jpeg"})))
        (.delete (io/file "test/fixtures/asset-cache/assets/images/dieter-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg"))))))
