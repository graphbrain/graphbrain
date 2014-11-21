(ns graphbrain.web.encoder)

(defn encode
  [str]
  (clojure.string/replace
   (clojure.string/replace str "#" "##")
   "'" "#1"))
