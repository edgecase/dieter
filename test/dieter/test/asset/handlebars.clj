(ns dieter.test.asset.handlebars
  (:use dieter.asset.handlebars)
  (:use clojure.test)
  (:use dieter.settings)
  (:use dieter.test.helpers)
  (:require [clojure.java.io :as io]))

(deftest test-preprocess-handlebars
  (testing "ember mode"
    (binding [*settings* (merge *settings* {:hbs-mode :ember})]
      (is (has-text? (preprocess-handlebars (io/file "test/fixtures/assets/javascripts/view.hbs"))
                     "Ember.TEMPLATES[\"view\"]=Ember.Handlebars.template"))))
  (testing "handlebars mode"
    (binding [*settings* (merge *settings* {:hbs-mode :handlebars})]
      (is (has-text? (preprocess-handlebars (io/file "test/fixtures/assets/javascripts/view.hbs"))
                     "Handlebars.templates[\"view\"]=Handlebars.template")))))