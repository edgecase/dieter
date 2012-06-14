(ns dieter.test.asset.css
  (:use dieter.asset.css)
  (:import dieter.asset.css.Css)
  (:use dieter.asset)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-read-asset-css
  (let [asset (read-asset (Css. (io/file "test/fixtures/assets/stylesheets/main.css") nil) {})]
    (testing "adds a source comment"
      (is (has-text? (:content asset) "/* Source: test/fixtures/assets/stylesheets/main.css */")))
    (testing "includes file contents"
      (is (has-text? (:content asset) "text-decoration: blink;")))))

(deftest test-compress-css
  (let [uncompressed-css "   .content .p {\n color: #fff;\n }"
        asset (Css. "filename.css" uncompressed-css)]
    (testing "compression disabled"
      (is (= uncompressed-css
             (compress asset {:compress false}))))

    (testing "compression enabled"
      (is (= ".content .p { color: #fff; }"
             (compress asset {:compress true}))))))