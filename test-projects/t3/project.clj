(defproject t3 "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :main t3.core/boot
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.1.1"]
                 [ring/ring-jetty-adapter "1.1.1"]
                 [org.eclipse.jetty/jetty-server "7.6.1.v20120215"]
                 [dieter/dieter "0.2.2"]]
  :dev-dependencies [[lein-dieter-precompile "0.1.3"]]
  :dieter-options {:cache-mode :production}
  :jvm-opts ["-Djna.library.path=native/macosx/x86_64:native/linux/x86_64:native/linux/x86:"])

