(ns dieter.test.asset.coffeescript
  (:use dieter.asset.coffeescript)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-preprocess-coffeescript
  (testing "basic coffee file"
    (is (= "(function() {\n\n  (function(param) {\n    return alert(\"x\");\n  });\n\n}).call(this);\n"
           (preprocess-coffeescript
            (io/file "test/fixtures/assets/javascripts/test.js.coffee")))))
  (testing "syntax error"
    (try
      (let [output (preprocess-coffeescript
                    (io/file "test/fixtures/assets/javascripts/bad.js.coffee"))]

        (is (has-text? (.toString output) "throw"))
        (is (has-text? (.toString output) "on line 2"))
        (is (has-text? (.toString output) "bad.js.coffee"))
        (is (has-text? (.toString output) "unmatched ]"))))))