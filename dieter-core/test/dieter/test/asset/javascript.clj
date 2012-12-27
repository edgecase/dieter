(ns dieter.test.asset.javascript
  (:require [clojure.java.io :as io]
            [dieter.test.helpers :as h]
            [dieter.asset :as asset]
            [dieter.asset.javascript :as js])
  (:use clojure.test)
  (:import dieter.asset.javascript.Js))

(deftest test-read-asset-js
  (let [asset (asset/read-asset (Js. (io/file "test/fixtures/assets/javascripts/app.js") nil) {})]
    (testing "adds a source comment"
      (is (h/has-text? (:content asset) "/* Source: test/fixtures/assets/javascripts/app.js */")))
    (testing "includes file contents"
      (is (h/has-text? (:content asset) "var file = \"/app.js\"")))))

(deftest test-compress-js
  (testing "valid javascript"
    (let [uncompressed-js " var foo = 'bar'; "
          asset (Js. "filename.js" uncompressed-js)]
      (testing "compression disabled"
        (is (= uncompressed-js
               (asset/compress asset {:compress false}))))

      (testing "compression enabled"
        (is (= "var foo=\"bar\";"
               (asset/compress asset {:compress true}))))))

  (testing "with compile errors"
    (let [uncompressed-with-errors "var foo = [1, 2, 3, ];"
          asset (Js. "haz-errors.js" uncompressed-with-errors)]
      (is (= uncompressed-with-errors
             (asset/compress asset {:compress true :log-level :quiet}))))))
