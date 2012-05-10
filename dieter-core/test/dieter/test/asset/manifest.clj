(ns dieter.test.asset.manifest
  (:use dieter.asset.manifest)
  (:use dieter.asset)
  (:use clojure.test)
  (:use dieter.test.helpers)
  (:require dieter.core)
  (:require [clojure.java.io :as io]))

(defn contains-file? [seq file]
  (<= 1 (count (filter #(= (.getCanonicalPath %) (.getCanonicalPath file)) seq))))

(deftest test-manifest-files
  (let [manifest (io/file "test/fixtures/assets/javascripts/manifest.js.dieter")
        files (manifest-files manifest)]
    (is (contains-file? files (io/file "test/fixtures/assets/javascripts/app.js")))
    (is (contains-file? files (io/file "test/fixtures/assets/javascripts/lib/framework.js")))
    (is (contains-file? files (io/file "test/fixtures/assets/javascripts/lib/dquery.js")))
    (is (contains-file? files (io/file "test/fixtures/assets/javascripts/models/feature.js")))
    (is (not (contains-file? files manifest)))

    (testing "load javascript file with same name as directory to be loaded"
      (is (contains-file? files (io/file "test/fixtures/assets/javascripts/lib.js"))))))

(deftest test-dieter-asset
  (let [manifest (make-asset (io/file "test/fixtures/assets/javascripts/manifest.js.dieter"))
        text (str (:content (read-asset manifest {})))]

    (testing "relative file paths"
      (is (has-text? text "var file = \"/app.js\"")))

    (testing "non-specific file paths"
      (is (has-text? text "var file = \"/lib/framework.js\"")))

    (testing "comments indicating original file source"
      (is (has-text? text "/* Source: test/fixtures/assets/javascripts/lib/framework.js */")))

    (testing "trailing slash requires all files under that directory"
      (is (has-text? text "var file = \"/lib/dquery.js\"")))

    (testing "multiple requires are included only once, the first occurrence"
      (is (has-text? text "var file = \"/lib/framework.js\"" 1)))))


(deftest test-emacs-file
  (let [manifest (io/file "test/fixtures/assets/javascripts/emacs_test/emacs.js.dieter")
        files (manifest-files manifest)]
    (is (not (contains-file? files (io/file "test/fixtures/assets/javascripts/emacs_test/nested/.#testfile.coffee"))))))
