(defproject dieter "0.0.1-SNAPSHOT"
  :description "(inc sprockets)"
  :repositories {"local" ~(str (.toURI (java.io.File. "maven_repository")))}
  :dependencies [[ring "1.0.1"]
                [yuicompressor "2.4.7"]
                [rhino/js "1.7R2"]
                 [org.clojure/clojure "1.3.0"]])

