(ns graphbrain.eco.words
  (:use graphbrain.eco.word)
  (:import (com.graphbrain.eco Words)))

(defn words-obj->words
  [words-obj]
  (map word-obj->word (. words-obj getWords)))

(defn str->words
  [s]
  (words-obj->words (Words/fromString s)))
