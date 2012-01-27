(ns dieter.preprocessors.less
  (:import [com.googlecode.lesscss4j LessCompiler LessException]))

(defn compiler []
  (LessCompiler.))

(defn preprocess-less [file]
  (try
    (.compile (compiler) file)
    (catch LessException e
      (str "/* " (.getMessage e) " */"))
    (catch RuntimeException e
      (str "/* " (.getMessage e) " */"))))
