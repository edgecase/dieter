(ns dieter.asset.css
  (:use
   [dieter.asset :only [register]]
   [dieter.util :only [slurp-into string-builder]]
   [dieter.compressor :only [compress-css]]))

(defrecord Css [file content]
  dieter.asset.Asset
  (read-asset [this options]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (.getCanonicalPath (:file this)) " */\n")
            (:file this))))

  dieter.asset.Compressor
  (compress [this options]
    (if (:compress options)
      (compress-css (:content this))
      (:content this))))

(register "css" map->Css)