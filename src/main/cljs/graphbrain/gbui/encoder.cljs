(ns graphbrain.gbui.encoder)

(defn encode
  [str]
  (clojure.string/replace
   (clojure.string/replace str "#" "##")
   "'" "#1"))

(defn- next-char
  [c0 c]
  (case c
    \# (if (nil? c0) \# nil)
    \1 (if (nil? c0) \' 1)
    c))

(defn- decode-char
  [str c]
  (conj str
        (next-char (first str) c)))

(defn decode
  [s]
  (apply str
         (reverse
          (reduce decode-char '() s))))
