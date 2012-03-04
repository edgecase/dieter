(ns dieter.test.core
  (:use dieter.core)
  (:use dieter.settings)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(comment test-preprocess-dieter
  (let [manifest (io/file "test/fixtures/assets/javascripts/manifest.js.dieter")
        text (preprocess-file manifest)]

    (testing "relative file paths"
      (is (has-text? text "var file = \"/app.js\"")))

    (testing "non-specific file paths"
      (is (has-text? text "var file = \"/lib/framework.js\"")))

    (testing "comments indicating original file source"
      (is (has-text? text "/* Source: test/fixtures/assets/javascripts/lib/framework.js */")))

    (testing "trailing slash requires all files under that directory"
      (is (has-text? text "var file = \"/lib/dquery.js\"")))

    (testing "multiple requires are included only once, the first occurrence"
      (is (has-text? text "var file = \"/lib/framework.js\"" 1)))))

(comment test-compress
  (let [uncompressed-js " var foo = 'bar'; "
        uncompressed-css "   .content .p {\n color: #fff;\n }"]
    (testing "compression disabled"
      (binding [*settings* {:compress false}]
        (is (= uncompressed-js
               (compress uncompressed-js "foo.js")))
        (is (= uncompressed-css
               (compress uncompressed-css "foo.css")))))

    (testing "compression enabled"
      (binding [*settings* {:compress true}]
        (is (= "var foo=\"bar\";"
               (compress uncompressed-js "foo.js")))
        (is (= ".content .p { color: #fff; }"
               (compress uncompressed-css "foo.css")))
        (testing "with compile errors will not compress"
          (let [uncompressed-with-errors "var foo = [1, 2, 3, ];"]
            (is (= uncompressed-with-errors
                 (compress uncompressed-with-errors "foo.js")))))))))

(deftest test-link-to-asset
  (testing "development mode"
    (let [opts {:cache-mode :development :asset-root "test/fixtures" :cache-root "test/fixtures/asset-cache"}]
      (is (= "/assets/javascripts/app.js" (link-to-asset "javascripts/app.js" opts)))
      (is (nil? (link-to-asset "javascripts/dontfindme.js" opts)))
      (is (= "/assets/javascripts/manifest.js" (link-to-asset "javascripts/manifest.js" opts)))))
  (testing "production mode"
    (let [opts {:cache-mode :production :asset-root "test/fixtures" :cache-root "test/fixtures/asset-cache"}]
      (testing "no previous file generated"
        (is (re-matches #"/assets/javascripts/app-[\da-f]{32}\.js" (link-to-asset "javascripts/app.js" opts))))
      (testing "file previously generated"
        (swap! cached-paths assoc "/assets/javascripts/app.js" "/assets/javascripts/app-12345678901234567890af1234567890.js")
        (is (= "/assets/javascripts/app-12345678901234567890af1234567890.js" (link-to-asset "javascripts/app.js" opts)))))))

(deftest test-write-to-cache
  (binding [*settings* (merge *settings* {:asset-root "test/fixtures", :cache-root "test/fixtures/asset-cache"})]
    (let [content "var aString = 'of javascript';"
          request-path "./assets/javascripts/awesomesauce.js"
          cache-path (write-to-cache content request-path)]
      (is (= "./test/fixtures/asset-cache/assets/javascripts/awesomesauce-8be397d9c4a3c4ad35f33963fedad96b.js" (str cache-path)))
      (is (= content (slurp cache-path)))
      (.delete cache-path))))

(deftest test-asset-builder
  (let [app (fn [req] (:uri req))
        options {:asset-root "test/fixtures", :cache-root "test/fixtures/asset-cache"}
        builder (asset-builder app options)]
    (testing "plain file paths"
      (reset! cached-paths {})
      (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js" (builder {:uri "/assets/javascripts/app.js"})))
      (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js" (get @cached-paths "/assets/javascripts/app.js")))
      (.delete (io/file "test/fixtures/asset-cache/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js")))
    (testing "md5'd file paths"
      (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js" (builder {:uri "/assets/javascripts/app-12345678901234567890123456789012.js"})))
      (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js" (get @cached-paths "/assets/javascripts/app.js")))
      (.delete (io/file "test/fixtures/asset-cache/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js")))))
