(ns dieter.test.asset.hamlcoffee
  (:use dieter.asset.hamlcoffee)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(defn wrap [name output]
  "The hamlcoffee compiler wraps the code, so we need to wrap our expected output"
  (str "(function() {\n  var _ref;\n\n  if ((_ref = window.HAML) == null) {\n    window.HAML = {};\n  }\n\n  window.HAML['"
       name
       "'] = function(context) {\n    return (function() {\n      var $o;\n      $o = [];\n      $o.push(\""

       output
       "\");\n      return $o.join(\"\\n\");\n    }).call(context);\n  };\n\n}).call(this);\n"))

(deftest test-preprocess-hamlcoffee
  (with-both-engines
    (testing "basic hamlc file"
      (is (= (wrap "basic"  "<!DOCTYPE html>\\n<html>\\n  <head>\\n    <title>\\n      Title\\n    </title>\\n  </head>\\n  <body>\\n    <h1>\\n      Header\\n    </h1>\\n  </body>\\n</html>")
             (preprocess-hamlcoffee
              (io/file "test/fixtures/assets/javascripts/basic.hamlc")))))))

(deftest test-caching
  (with-both-engines
    (testing "we get the same result when there are caches"
      (is (= (preprocess-hamlcoffee
              (io/file "test/fixtures/assets/javascripts/basic.hamlc"))
             (preprocess-hamlcoffee
              (io/file "test/fixtures/assets/javascripts/basic.hamlc"))
             (preprocess-hamlcoffee
              (io/file "test/fixtures/assets/javascripts/basic.hamlc"))
             (preprocess-hamlcoffee
              (io/file "test/fixtures/assets/javascripts/basic.hamlc")))))))

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