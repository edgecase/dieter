(ns leiningen.dieter-precompile
  "Precompile dieter assets"
  (:use [leiningen.core.eval :only (eval-in-project)]))


(defn dieter-precompile
  [project]
  (eval-in-project
   project
   `(do (leiningen.dieter-precompile.runtime/precompile '~project))
   `(require 'leiningen.dieter-precompile.runtime)))