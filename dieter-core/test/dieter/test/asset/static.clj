(ns dieter.test.asset.static
  (:use dieter.asset.static)
  (:import dieter.asset.static.Static)
  (:use dieter.asset)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-static-assets
  (let [file (io/file "test/fixtures/assets/images/dieter.jpeg")
        asset (read-asset (Static. file nil) {})]
    (testing "read-asset"
      (is (= (.length file)
             (count (:content asset)))))
    (testing "compress returns unmodified content"
      (is (= (:content asset)
             (compress asset {:compress true}))))))