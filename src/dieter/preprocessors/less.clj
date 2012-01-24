(ns dieter.preprocessors.less
  (:import [com.googlecode.lesscss4j LessCompiler LessException]))

(def compiler (LessCompiler.))

(defn preprocess-less [file]
  (try
    (.compile compiler file)
    (catch LessException e
      (str "/* " (.getMessage e) " */"))))
