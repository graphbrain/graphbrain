(ns graphbrain.eco.word
  (:use [graphbrain.utils :only [dbg]])
  (:import (com.graphbrain.eco Word)))

(defn pos-first-char-is?
  [word prefix]
  (let [first-char (str (first (:pos word)))]
    (= first-char prefix)))

(defn noun?
  [word]
  (pos-first-char-is? word "N"))

(defn adjective?
  [word]
  (pos-first-char-is? word "J"))

(defn comparative?
  [word]
  (= (:pos word) "JJR"))

(defn verb?
  [word]
  (or (pos-first-char-is? word "V")
      (= (:pos word) "MD")))

(defn indicator?
  [word]
  (pos-first-char-is? word "I"))

(defn adverb?
  [word]
  (.startsWith (:pos word) "RB"))

(defn det?
  [word]
  (= (:pos word) "DT"))

(defn to?
  [word]
  (= (:pos word) "TO"))

(defn word-obj->word
  [word-obj]
  {:word (clojure.string/lower-case (. word-obj getWord))
   :pos (. word-obj getPos)
   :lemma (. word-obj getLemma)})
