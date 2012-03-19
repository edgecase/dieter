(ns dieter.asset.javascript
  (:use
   [dieter.asset :only [register]]
   [dieter.util :only [slurp-into string-builder]]
   [dieter.compressor :only [compress-js]]))

(defrecord Js [file content]
  dieter.asset.Asset
  (read-asset [this options]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (:file this) " */\n")
            (:file this))))

  dieter.asset.Compressor
  (compress [this options]
    (if (:compress options)
      (compress-js (:content this))
      (:content this))))

(register "js" map->Js)