(ns dieter.test.preprocessors.coffeescript
  (:use dieter.preprocessors.coffeescript)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-preprocess-coffeescript
  (testing "basic coffee file"
    (is (= "(function() {\n  (function(param) {\n    return alert(\"x\");\n  });\n}).call(this);\n"
           (preprocess-coffeescript
            (io/file "test/fixtures/assets/javascripts/test.js.coffee"))))))