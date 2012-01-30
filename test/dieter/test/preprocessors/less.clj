(ns dieter.test.preprocessors.less
  (:use dieter.preprocessors.less)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-preprocess-less
  (testing "basic less file"
    (is (= "#header {\n  color: #4d926f;\n}\n" (preprocess-less (io/file "test/fixtures/assets/stylesheets/basic.less")))))
  (testing "file with imports"
    (is (= "#includee {\n  color: white;\n}\n#includer {\n  color: black;\n}\n"
           (preprocess-less (io/file "test/fixtures/assets/stylesheets/includes.less")))))
  (testing "bad less syntax"
    (is (has-text? (preprocess-less (io/file "test/fixtures/assets/stylesheets/bad.less")) "ERROR: Syntax Error on line 1"))
    (is (has-text? (preprocess-less (io/file "test/fixtures/assets/stylesheets/bad.less")) "@import \"includeme.less\""))))
