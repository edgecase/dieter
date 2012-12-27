(ns dieter.test.asset.css
  (:require [clojure.java.io :as io]
            [dieter.test.helpers :as h]
            [dieter.asset :as asset]
            [dieter.asset.css :as css])
  (:use clojure.test)
  (:import dieter.asset.css.Css))

(deftest test-read-asset-css
  (let [asset (asset/read-asset (Css.
                                 (io/file "test/fixtures/assets/stylesheets/main.css")
                                 nil) {})]
    (testing "adds a source comment"
      (is (h/has-text? (:content asset) "/* Source: test/fixtures/assets/stylesheets/main.css */")))
    (testing "includes file contents"
      (is (h/has-text? (:content asset) "text-decoration: blink;")))))

(deftest test-compress-css
  (let [uncompressed-css "   .content .p {\n color: #fff;\n }"
        asset (Css. "filename.css" uncompressed-css)]
    (testing "compression disabled"
      (is (= uncompressed-css
             (asset/compress asset {:compress false}))))

    (testing "compression enabled"
      (is (= ".content .p { color: #fff; }"
             (asset/compress asset {:compress true}))))))