(ns graphbrain.braingenerators.nlptools
  (:require [graphbrain.braingenerators.htmltools :as htmltools]
            [graphbrain.eco.words :as words])
  (:import (java.io StringReader)
           (edu.stanford.nlp.process DocumentPreprocessor)
           (edu.stanford.nlp.ling HasWord)
           (com.graphbrain.eco Words POSTagger)))

(defn- has-word-list->sentence
  [word-list]
  (clojure.string/join " "
    (map (fn [w] (. w toString)) word-list)))

(defmulti extract-sentences class)

(defmethod extract-sentences java.lang.String
  [text]
  (let
    [dp (new DocumentPreprocessor (new StringReader text))]
    (map (fn [l] (has-word-list->sentence l)) dp)))

(defmethod extract-sentences clojure.lang.Sequential
  [text-parts]
  (flatten (map extract-sentences text-parts)))

(defn sentences->words
  [sentences]
  (flatten sentences))

(defn html->sentences
  [html]
  (map words/str->words
       (extract-sentences ((htmltools/html->text+tags html) :text-parts))))

(defn html->words
  [html]
  (sentences->words
   (extract-sentences ((htmltools/html->text+tags html) :text))))

(defn print-sentences
  [sentences]
  (doseq [sentence sentences]
    (prn (clojure.string/join " " (map #(:word %) sentence)))))
