(defproject dieter "0.2.2"
  :description "Asset pipeline ring middleware"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring "1.0.1"]
                 [fs "0.11.1"]
                 [com.google.javascript/closure-compiler "r1592"]
                 [clj-v8 "0.1.1"]
                 [clj-v8-native "0.1.1"]
                 [rhino/js "1.7R2"]]
  :dev-dependencies [[org.clojure/clojure "1.3.0"]
                     [redd/native-deps "1.0.7"]]
  :jvm-opts ["-Djna.library.path=native/macosx/x86_64:native/linux/x86_64:native/linux/x86:"])


