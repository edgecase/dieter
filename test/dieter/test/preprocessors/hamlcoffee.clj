(ns dieter.test.preprocessors.hamlcoffee
  (:use dieter.preprocessors.hamlcoffee)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))


(deftest test-preprocess-hamlcoffee
  (testing "basic hamlc file"
    (is (= "TODO"
           (preprocess-hamlcoffee
            (io/file "test/fixtures/assets/javascripts/basic.hamlc"))))))

  ;; (testing "file with partials"
  ;;   (is (= "TODO"
  ;;          (preprocess-hamlcoffee
  ;;           (io/file "test/fixtures/assets/javascripts/with-partials.hamlc")))))

  ;; (testing "file with surround and succeed"
  ;;   (is (= "TODO"
  ;;          (preprocess-hamlcoffee
  ;;           (io/file "test/fixtures/assets/javascripts/with-partials.hamlc")))))

;; (testing "file with coffeescript"
  ;;   (is (= "TODO"
  ;;          (preprocess-hamlcoffee
  ;;           (io/file "test/fixtures/assets/javascripts/with-coffee.hamlc")))))

  ;; (testing "bad haml syntax"
  ;;   (is (has-text?
  ;;        (preprocess-hamlcoffee
  ;;         (io/file "test/fixtures/assets/javascripts/badhaml1.hamlc"))
  ;;        "ERROR: Syntax Error on line 1"))

  ;;   (is (has-text?
  ;;        (preprocess-hamlcoffee
  ;;         (io/file "test/fixtures/assets/javascripts/badhaml2.hamlc"))
  ;;        "@import \"includeme.less\"")))

  ;; (testing "bad coffee syntax"
  ;;   (is (has-text?
  ;;        (preprocess-hamlcoffee
  ;;         (io/file "test/fixtures/assets/javascripts/badcoffee.hamlc"))
  ;;        "@import \"includeme.less\""))))