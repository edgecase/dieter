(ns leiningen.dieter-precompile
  "Precompile dieter assets"
  (:use [leiningen.compile :only (eval-in-project)]))


(defn dieter-precompile
  [project]
  (eval-in-project
   project
   `(do (leiningen.dieter-precompile.runtime/precompile '~project))
   nil
   nil
   `(require 'leiningen.dieter-precompile.runtime)))