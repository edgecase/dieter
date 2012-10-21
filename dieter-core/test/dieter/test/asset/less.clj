(ns dieter.test.asset.less
  (:use dieter.asset.less)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-preprocess-less
  (testing "basic less file"
    (is (= "#header {\n  color: #4d926f;\n}\n" (preprocess-less (io/file "test/fixtures/assets/stylesheets/basic.less")))))
  (testing "file with imports"
    (is (= "#includee {\n  color: white;\n}\n#includee-three {\n  color: white;\n}\n#includee-two {\n  color: white;\n}\n#includer {\n  color: black;\n}\n"
           (preprocess-less (io/file "test/fixtures/assets/stylesheets/includes.less")))))
  (testing "bad less syntax"
    (let [output (preprocess-less (io/file "test/fixtures/assets/stylesheets/bad.less"))]
                                        ; test it throws
      (is (has-text? output "Syntax Error on line 1"))
      (is (has-text? output "@import \\\"includeme.less\\\"")))))
