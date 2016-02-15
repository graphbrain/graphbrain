;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

(ns graphbrain.kr.nlptools
  (:require [graphbrain.kr.htmltools :as htmltools]
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
