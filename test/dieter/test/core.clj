(ns dieter.test.core
  (:use dieter.core)
  (:use clojure.test)
  (:require [clojure.java.io :as io]))

(defn has-text?
  ([text expected]
     (not= -1 (.indexOf text expected)))
  ([text expected times]
     (= times (count (re-seq (re-pattern expected) text)))))

(deftest test-cache-path
  (is (= "/resources/asset-cache/assets/foo.js"
         (cache-path "/assets/foo.js"))))

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
        text (preprocess-dieter manifest)]

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
