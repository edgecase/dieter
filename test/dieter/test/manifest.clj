(ns dieter.test.manifest
  (:use dieter.manifest)
  (:use clojure.test)
  (:require [clojure.java.io :as io]))

(defn contains-file? [seq file]
  (<= 1 (count (filter #(= (.getCanonicalPath %) (.getCanonicalPath file)) seq))))

(deftest test-manifest-files
  (let [manifest (io/file "test/fixtures/assets/javascripts/manifest.js.dieter")
        files (manifest-files manifest)]
    (is (contains-file? files (io/file "test/fixtures/assets/javascripts/app.js")))
    
    (testing "load javascript file with same name as directory to be loaded"
      (is (contains-file? files (io/file "test/fixtures/assets/javascripts/lib.js"))))
    
    (is (contains-file? files (io/file "test/fixtures/assets/javascripts/lib/framework.js")))
    (is (contains-file? files (io/file "test/fixtures/assets/javascripts/lib/dquery.js")))
    (is (contains-file? files (io/file "test/fixtures/assets/javascripts/models/feature.js")))
    (is (not (contains-file? files manifest)))))