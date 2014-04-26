(ns dieter.test.asset.livescript
  (:require [dieter.test.helpers :as h]
            [dieter.asset.livescript :as ls]
            [clojure.java.io :as io])
  (:use clojure.test))

(deftest test-preprocess-livescript
  (h/with-both-engines
    (let [fixture (io/file "test/fixtures/assets/javascripts/test.js.ls")
          bad-file (io/file "test/fixtures/assets/javascripts/bad.js.ls")]
      (testing "we have a chance to succeed"
        (is (.exists fixture)))
      (testing "basic ls file"
        (let [expected (clojure.string/join "\n" ["(function(){"
                                                  "  (function(it){"
                                                  "    return it + \"!\";"
                                                  "  })("
                                                  "  function(it){"
                                                  "    return it.x;"
                                                  "  }("
                                                  "  {"
                                                  "    x: 'foo'"
                                                  "  }));"
                                                  "}).call(this);"
                                                  ""])]
          (is (= expected
                 (ls/preprocess-livescript fixture)))))
      (testing "syntax error"
        (try
          (ls/preprocess-livescript bad-file)
          (is false) ; must throw
          (catch Exception e
            (is (h/has-text? (.toString e) "line 1"))
            (is (h/has-text? (.toString e) "unmatched"))))))))
