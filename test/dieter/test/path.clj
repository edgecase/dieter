(ns dieter.test.path
  (:use dieter.path)
  (:use clojure.test)
  (:require [clojure.java.io :as io]))

(deftest test-cached-file-path
  (is (= "/resources/asset-cache/assets/foo-d259b08a2dfaf8bf776cbadbe85442d3.js"
         (cached-file-path "/assets/foo.js" "content string"))))

(deftest test-find-file
  (let [dir (io/file "test/fixtures/assets/javascripts/")]
    (testing "relative path"
      (let [file (find-file "./lib/framework.js" dir)]
        (is (re-matches #".*test/fixtures/assets/javascripts/(\./)?lib/framework.js$" (.getPath file)))
        (is (.exists file)))
      (is (nil? (find-file "./framework.js" dir))))

    (testing "non-specific path"
      (let [file (find-file "framework.js" dir)]
        (is (re-matches #".*test/fixtures/assets/javascripts/lib/framework.js$" (.getPath file)))
        (is (.exists file))))

    (testing "partial path"
      (let [file (find-file "lib/framework.js" dir)]
        (is (re-matches #".*test/fixtures/assets/javascripts/lib/framework.js$" (.getPath file)))
        (is (.exists file))))

    (testing "no file extension"
      (let [file (find-file "framework" dir)]
        (is (re-matches #".*test/fixtures/assets/javascripts/lib/framework.js$" (.getPath file)))
        (is (.exists file))))

    (testing "different file extension"
      (let [file (find-file "basic.css" (io/file "test/fixtures/assets/stylesheets/"))]
        (is (re-matches #".*test/fixtures/assets/stylesheets/basic.less$" (.getPath file)))
        (is (.exists file))))

    (testing "no file exists"
      (is (nil? (find-file "dontfindme.txt" dir))))))

(defn file= [f1 f2]
  (= (.getCanonicalPath (io/file f1))
     (.getCanonicalPath (io/file f2))))

(deftest test-search-dir
  (is (file= "test/fixtures/assets/javascripts/lib/"
             (search-dir "lib/framework.js" (io/file "test/fixtures/assets/javascripts/"))))
  (is (file= "test/fixtures/assets/javascripts/"
             (search-dir "./app.js" (io/file "test/fixtures/assets/javascripts/"))))
  (is (file= "test/fixtures/assets/javascripts/lib/"
             (search-dir "./lib/" (io/file "test/fixtures/assets/javascripts/"))))
  (is (file= "test/fixtures/assets/javascripts/lib/"
             (search-dir "./lib/" (.getParentFile (io/file "test/fixtures/assets/javascripts/manifest.js.dieter")))))
  (is (file= "test/fixtures/assets/javascripts/models/"
             (search-dir "./models/" (.getParentFile (io/file "test/fixtures/assets/javascripts/manifest.js.dieter"))))))

(deftest test-md5
  (is (= "acbd18db4cc2f85cedef654fccc4a4d8" (md5 "foo")))
  (testing "leading zeros should be included"
    (is (= "02b97f7bc37b2b68fc847fcc3fc1c156" (md5 "foooo")))))

(deftest test-add-md5
  (is (= "/assets/foo-acbd18db4cc2f85cedef654fccc4a4d8.js" (add-md5 "/assets/foo.js" "foo")))
  (is (= "/assets/foo.js-acbd18db4cc2f85cedef654fccc4a4d8.txt" (add-md5 "/assets/foo.js.txt" "foo")))
  (is (= "/assets/foo-acbd18db4cc2f85cedef654fccc4a4d8" (add-md5 "/assets/foo" "foo"))))

(deftest test-uncachfy-filename
  (is (= "/assets/foo.js" (uncachify-filename "/assets/foo-acbd18db4cc2f85cedef654fccc4a4d8.js")))
  (is (= "/assets/foo.js.txt" (uncachify-filename "/assets/foo.js-acbd18db4cc2f85cedef654fccc4a4d8.txt")))
  (is (= "/assets/foo.js" (uncachify-filename "/assets/foo.js"))))

(deftest test-make-relative-to-cache
  (is (= "/assets/javascripts/awesomesauce-8be397d9c4a3c4ad35f33963fedad96b.js"
         (make-relative-to-cache "./resources/asset-cache/assets/javascripts/awesomesauce-8be397d9c4a3c4ad35f33963fedad96b.js"))))

