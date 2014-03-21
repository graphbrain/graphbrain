(ns graphbrain.braingenerators.nlptools
  (:use [clojure.string :only [join]]
        graphbrain.braingenerators.htmltools
        graphbrain.eco.words)
  (:import (java.io StringReader)
           (edu.stanford.nlp.process DocumentPreprocessor)
           (edu.stanford.nlp.ling HasWord)
           (com.graphbrain.eco Words POSTagger)))

(defn- has-word-list->sentence
  [word-list]
  (join " "
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

(defn url->sentences
  [url-str]
  (map str->words
       (extract-sentences ((url->text+tags url-str) :text-parts))))

(defn url->words
  [url-str]
  (sentences->words
   (extract-sentences ((url->text+tags url-str) :text))))

(defn print-sentences
  [sentences]
  (doseq [sentence sentences]
    (prn (clojure.string/join " " (map #(:word %) sentence)))))
