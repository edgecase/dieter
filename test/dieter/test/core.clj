(ns dieter.test.core
  (:use dieter.core)
  (:use clojure.test)
  (:require [clojure.java.io :as io]))

(defn has-text?
  "returns true if expected occurs in text exactly n times (one or more times if not specified)"
  ([text expected]
     (not= -1 (.indexOf text expected)))
  ([text expected times]
     (= times (count (re-seq (re-pattern expected) text)))))

(deftest test-cached-file-path
  (is (= "/resources/asset-cache/assets/foo-d259b08a2dfaf8bf776cbadbe85442d3.js"
         (cached-file-path "/assets/foo.js" "content string"))))

(deftest test-find-file
  (let [dir (io/file "test/fixtures/assets/javascripts/")]
    (testing "relative path"
      (let [file (find-file "./lib/framework.js" dir)]
        (is (re-matches #".*test/fixtures/assets/javascripts/(\./)?lib/framework.js$" (.getPath file)))
        (is (.exists file)))
      (is (nil? (find-file "./framework.js" dir))))

    (testing "non-specific path"
      (let [file (find-file "framework.js" dir)]
        (is (re-matches #".*test/fixtures/assets/javascripts/lib/framework.js$" (.getPath file)))
        (is (.exists file))))

    (testing "partial path"
      (let [file (find-file "lib/framework.js" dir)]
        (is (re-matches #".*test/fixtures/assets/javascripts/lib/framework.js$" (.getPath file)))
        (is (.exists file))))

    (testing "no file exists"
      (is (nil? (find-file "dontfindme.txt" dir))))))

(deftest test-preprocess-dieter
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

(deftest test-compress
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
               (compress uncompressed-css "foo.css")))))))

(deftest test-search-dir
  (is (= (io/file "test/fixtures/assets/javascripts/lib/")
         (search-dir (io/file "lib/framework.js") (io/file "test/fixtures/assets/javascripts/"))))
  (is (= (io/file "test/fixtures/assets/javascripts/.")
         (search-dir (io/file "./app.js") (io/file "test/fixtures/assets/javascripts/"))))
  (is (= (io/file "test/fixtures/assets/javascripts/./lib/")
         (search-dir (io/file "./lib/") (io/file "test/fixtures/assets/javascripts/")))))

(deftest test-md5
  (is (= "acbd18db4cc2f85cedef654fccc4a4d8" (md5 "foo")))
  (testing "leading zeros should be included"
    (is (= "02b97f7bc37b2b68fc847fcc3fc1c156" (md5 "foooo")))))

(deftest test-add-md5
  (is (= "/assets/foo-acbd18db4cc2f85cedef654fccc4a4d8.js" (add-md5 "/assets/foo.js" "foo")))
  (is (= "/assets/foo.js-acbd18db4cc2f85cedef654fccc4a4d8.txt" (add-md5 "/assets/foo.js.txt" "foo")))
  (is (= "/assets/foo-acbd18db4cc2f85cedef654fccc4a4d8" (add-md5 "/assets/foo" "foo"))))

(deftest test-link-to-asset
  (testing "development mode"
    (let [opts {:cache-mode :development :asset-root "test/fixtures" :cache-root "test/fixtures/asset-cache"}]
      (is (= "/assets/javascripts/app.js" (link-to-asset "javascripts/app.js" opts)))
      (is (nil? (link-to-asset "javascripts/dontfindme.js" opts)))))
  (testing "production mode"
    (let [opts {:cache-mode :production :asset-root "test/fixtures" :cache-root "test/fixtures/asset-cache"}]
      (testing "no previous file generated"
        (is (re-matches #"/assets/javascripts/app-[\da-f]{32}\.js" (link-to-asset "javascripts/app.js" opts))))
      (testing "file previously generated"
        (swap! cached-paths assoc "/assets/javascripts/app.js" "/assets/javascripts/app-12345678901234567890af1234567890.js")
        (is  (link-to-asset "javascripts/app.js" opts))))))

(deftest test-write-to-cache
  (binding [*settings* (merge *settings* {:asset-root "test/fixtures", :cache-root "test/fixtures/asset-cache"})]
    (let [content "var aString = 'of javascript';"
          request-path "./assets/javascripts/awesomesauce.js"
          cache-path (write-to-cache content request-path)]
      (is (= "./test/fixtures/asset-cache/assets/javascripts/awesomesauce-8be397d9c4a3c4ad35f33963fedad96b.js" (str cache-path)))
      (is (= content (slurp cache-path)))
      (.delete cache-path))))

(deftest test-uncachfy-filename
  (is (= "/assets/foo.js" (uncachify-filename "/assets/foo-acbd18db4cc2f85cedef654fccc4a4d8.js")))
  (is (= "/assets/foo.js.txt" (uncachify-filename "/assets/foo.js-acbd18db4cc2f85cedef654fccc4a4d8.txt")))
  (is (= "/assets/foo.js" (uncachify-filename "/assets/foo.js"))))

(deftest test-make-relative-to-cache
  (is (= "/assets/javascripts/awesomesauce-8be397d9c4a3c4ad35f33963fedad96b.js"
         (make-relative-to-cache "./resources/asset-cache/assets/javascripts/awesomesauce-8be397d9c4a3c4ad35f33963fedad96b.js"))))

(deftest test-asset-pipeline
  (let [app (fn [req] (:uri req))
        options {:asset-root "test/fixtures", :cache-root "test/fixtures/asset-cache"}
        pipeline (asset-pipeline app options)]
    (reset! cached-paths {})
    (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js" (pipeline {:uri "/assets/javascripts/app.js"})))
    (is (= "/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js" (get @cached-paths "/assets/javascripts/app.js")))
    (.delete (io/file "test/fixtures/asset-cache/assets/javascripts/app-48587d6fc68f221f8fa67a63f4bb4b09.js"))))