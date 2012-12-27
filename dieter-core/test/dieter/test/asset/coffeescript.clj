(ns dieter.test.asset.coffeescript
  (:require [dieter.test.helpers :as h]
            [dieter.asset.coffeescript :as cs]
            [clojure.java.io :as io])
  (:use clojure.test))

(deftest test-preprocess-coffeescript
  (h/with-both-engines
    (testing "basic coffee file"
      (is (= "(function() {\n\n  (function(param) {\n    return alert(\"x\");\n  });\n\n}).call(this);\n"
             (cs/preprocess-coffeescript
              (io/file "test/fixtures/assets/javascripts/test.js.coffee")))))
    (testing "syntax error"
      (try
        (cs/preprocess-coffeescript
         (io/file "test/fixtures/assets/javascripts/bad.js.coffee"))
        (is false) ; must throw
        (catch Exception e
          (is (h/has-text? (.toString e) "on line 2"))
          (is (h/has-text? (.toString e) "unmatched ]")))))))