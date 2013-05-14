(defproject dieter "0.4.0"
  :description "Asset pipeline ring middleware"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "http://github.com/edgecase/dieter"
  :scm {:name "git" :dir ".."}
  :dependencies [[ring/ring-core "1.1.8"]
                 [clj-time "0.4.4"]
                 [com.google.javascript/closure-compiler "r1592"]
                 [clj-v8 "0.1.4"]
                 [clj-v8-native "0.1.4"]
                 [org.mozilla/rhino "1.7R4"]
                 [org.clojure/clojure "1.4.0"]]
  :dev-dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {:dev {:dependencies [[ring-mock "0.1.4"]]}})
