(ns dieter.test.asset.manifest
  (:require [dieter.asset.manifest :as manifest]
            [dieter.asset :as asset]
            [dieter.test.helpers :as h]
            [dieter.core :as core]
            [clojure.java.io :as io])
  (:use [clojure.test]))

(deftest test-manifest-files
  (let [manifest "test/fixtures/assets/javascripts/manifest.js.dieter"
        files (manifest/manifest-files (io/file manifest))]
    (is (h/contains-file? files "test/fixtures/assets/javascripts/app.js"))
    (is (h/contains-file? files "test/fixtures/assets/javascripts/lib/framework.js"))
    (is (h/contains-file? files "test/fixtures/assets/javascripts/lib/dquery.js"))
    (is (h/contains-file? files "test/fixtures/assets/javascripts/models/feature.js"))
    (is (not (h/contains-file? files manifest)))

    (testing "load javascript file with same name as directory to be loaded"
      (is (h/contains-file? files "test/fixtures/assets/javascripts/lib.js")))))

(deftest test-dieter-asset
  (let [manifest (asset/make-asset (io/file
                                    "test/fixtures/assets/javascripts/manifest.js.dieter"))
        text (str (:content (asset/read-asset manifest)))]

    (testing "relative file paths"
      (is (h/has-text? text "var file = \"/app.js\"")))

    (testing "non-specific file paths"
      (is (h/has-text? text "var file = \"/lib/framework.js\"")))

    (testing "comments indicating original file source"
      (is (h/has-text? text "/* Source: test/fixtures/assets/javascripts/lib/framework.js */")))

    (testing "trailing slash requires all files under that directory"
      (is (h/has-text? text "var file = \"/lib/dquery.js\"")))

    (testing "multiple requires are included only once, the first occurrence"
      (is (h/has-text? text "var file = \"/lib/framework.js\"")))))


(deftest test-emacs-file
  (let [manifest (io/file "test/fixtures/assets/javascripts/emacs_test/emacs.js.dieter")
        files (manifest/manifest-files manifest)]
    (is (not (h/contains-file?
              files "test/fixtures/assets/javascripts/emacs_test/nested/.#testfile.coffee")))))

(deftest test-vim-file
  (let [manifest (io/file "test/fixtures/assets/javascripts/vim_test/vim.js.dieter")
        files (manifest/manifest-files manifest)]
    (is (not (h/contains-file?
              files "test/fixtures/assets/javascripts/vim_test/nested/.testfile.coffee.swp")))))

(deftest test-nested-directories
  (let [manifest (io/file "test/fixtures/assets/javascripts/nested-dirs.js.dieter")
        files (manifest/manifest-files manifest)]
    (is (h/contains-file? files "test/fixtures/assets/javascripts/nested-dirs/nested1/a.js"))
    (is (h/contains-file? files "test/fixtures/assets/javascripts/nested-dirs/nested1/b.js"))
    (is (h/contains-file? files "test/fixtures/assets/javascripts/nested-dirs/nested2/c.js"))))

(deftest test-error-on-missing-file
  (let [filename "test/fixtures/assets/javascripts/missing_test/missing.dieter"
        manifest (io/file filename)]
    (try
      (manifest/manifest-files manifest)
      (is false) ; shouldnt hit
      (catch Exception e
        (is (h/has-text? (.toString e) (str "Cannot find some-file-which-doesnt-exist.js from " filename)))))))

(deftest test-files-named-same-as-dir
  ;; test for incorrect behaviour. When a dir A should contain a file A but doesn't, dieter returned the dir instead of the file.
  (let [filename "test/fixtures/assets/javascripts/missing_test/missing-in-dir.dieter"
        manifest (io/file filename)]
    (try
      (manifest/manifest-files manifest)
      (is false) ; shouldnt hit
      (catch Exception e
        (is (h/has-text? (.toString e) (str "Cannot find missing_file from " filename)))))))