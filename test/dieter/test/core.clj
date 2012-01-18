(ns dieter.test.core
  (:use dieter.core)
  (:use clojure.test)
  (:require [clojure.java.io :as io]))

(deftest test-cache-path
  (is (= "/resources/asset-cache/assets/foo.js"
         (cache-path "/assets/foo.js"))))

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

(deftest test-find-file
  (testing "relative path"
    (is (= "test/fixtures/assets/javascripts/app.js"
           (.getName (find-file "app.js" (io/file "test/fixtures/assets/javascripts/"))))))
  (testing "non-specific path")
  (testing "no file exists"
    (is (nil? (find-file "dontfindme.txt" (io/file "resources/"))))))

(deftest test-preprocess-file
  (testing "dieter")
  (testing "javascript")
  (testing "css")
  (testing "handlebars")
  (testing "less"))
