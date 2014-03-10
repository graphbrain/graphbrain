(ns graphbrain.eco.word
  (:import (com.graphbrain.eco Word)))

(defn pos-first-char-is?
  [word prefix]
  (let [first-char (str  (first (:pos word)))]
    (= first-char prefix)))

(defn noun?
  [word]
  (pos-first-char-is? word "N"))

(defn adjective?
  [word]
  (pos-first-char-is? word "J"))

(defn word-obj->word
  [word-obj]
  {:word (clojure.string/lower-case (. word-obj getWord))
   :pos (. word-obj getPos)
   :lemma (. word-obj getLemma)})
