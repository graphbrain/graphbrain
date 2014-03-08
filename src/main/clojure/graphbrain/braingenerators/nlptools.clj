(ns graphbrain.braingenerators.nlptools
  (:use [clojure.string :only [join]]
        graphbrain.braingenerators.htmltools)
  (:import (java.io StringReader)
           (edu.stanford.nlp.process DocumentPreprocessor)
           (edu.stanford.nlp.ling HasWord)
           (com.graphbrain.eco Words POSTagger)))

(defn- has-word-list->sentence
  [word-list]
  (join " "
    (map (fn [w] (. w toString)) word-list)))

(defn extract-sentences
  [text]
  (let
    [dp (new DocumentPreprocessor (new StringReader text))]
    (map (fn [l] (has-word-list->sentence l)) dp)))

(defn sentence->words
  "Convert sentence string to Words object"
  [sentence]
  (new Words
    (POSTagger/annotate sentence)))

(defn sentences->words-list
  [sentences]
  (map sentence->words sentences))

(defn words-list->word-list
  [words-list]
  (flatten (map
    (fn [words]
      (map (fn [word] word) (. words getWords))) words-list)))

(defn url->word-list
  [url-str]
  (words-list->word-list
   (sentences->words-list
    (extract-sentences ((url->text+tags url-str) :text)))))

(defn- test
  []
  (url->word-list "http://www.realclimate.org/index.php/archives/2014/03/the-nenana-ice-classic-and-climate/"))
