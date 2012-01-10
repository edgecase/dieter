(ns dieter.preprocessors.less
  (:use dieter.preprocessors.rhino))

(defn compile-less)

(def make-parser "new (less.Parser)({paths: ['./resources/assets/stylesheets']});")

(defn preprocess-less [file]
  (with-rhino context scope
    (load-vendor "less-1.2.0.js" context scope)
    (let [parser (.evaluateString context scope make-parser "make-parser" 1 nil)]
      parser)))

(preprocess-less (clojure.java.io/file "resources/assets/stylesheets/reset.less"))

;; var parser = new(less.Parser)({
;;     paths: ['.', './lib'], // Specify search paths for @import directives
;;     filename: 'style.less' // Specify a filename, for better error messages
;; });

;; parser.parse('.class { width: 1 + 1 }', function (e, tree) {
;;     tree.toCSS({ compress: true }); // Minify CSS output
;; });