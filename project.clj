(defproject dieter "0.0.1-SNAPSHOT"
  :description "(inc sprockets)"
  :repositories {"local" ~(str (.toURI (java.io.File. "maven_repository")))}
  :dependencies [[ring "1.0.1"]
                 [com.google.javascript/closure-compiler "r1592"]
                 [rhino/js "1.7R2"]]
  :dev-dependencies [[org.clojure/clojure "1.3.0"]])
