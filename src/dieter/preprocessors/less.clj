(ns dieter.preprocessors.less
  (:use dieter.preprocessors.rhino))

(defn preprocess-less [file]
  (with-rhino context scope
    (load-vendor "less-rhino-1.2.1.js" context scope)
    (load-vendor "less-wrapper.js" context scope)
    (let [less-str (slurp file)
          parser (.get scope "DieterParseLess" scope)]
      (.call parser context scope nil (into-array [less-str]))
      (Thread/sleep 500)
      (.get scope "DieterLastParse" scope))))

(comment
  (preprocess-less (clojure.java.io/file "resources/assets/stylesheets/reset.less"))

  (with-rhino context scope
    (load-vendor "less-1.2.0.js" context scope)
    (load-vendor "less-wrapper.js" context scope)
    (.get scope "DieterParseLess" scope)))



