(ns dieter.test.asset.static
  (:require [clojure.java.io :as io]
            [dieter.asset :as asset]
            [dieter.asset.static :as static])
  (:use clojure.test)
  (:import dieter.asset.static.Static))

(deftest test-static-assets
  (let [file (io/file "test/fixtures/assets/images/dieter.jpeg")
        asset (asset/read-asset (Static. file nil) {})]
    (testing "read-asset"
      (is (= (.length file)
             (count (:content asset)))))
    (testing "compress returns unmodified content"
      (is (= (:content asset)
             (asset/compress asset {:compress true}))))))