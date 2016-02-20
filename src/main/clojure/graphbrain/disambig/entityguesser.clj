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

(ns graphbrain.disambig.entityguesser
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.symbol :as symb]))

(defn substring?
  [sub st]
  (not= (.indexOf st sub) -1))

(defn participants
  [edge]
  (flatten
   (map #(if (coll? %) (rest %) %)
        (rest edge))))

(defn symbol->words
  [hg sym]
  (let [edges (ops/star hg sym)
        symbols (set (flatten (map participants edges)))
        concepts (filter #(= :concept (symb/sym-type %)) symbols)
        words (map symb/symbol->str concepts)
        words (map #(clojure.string/split % #" ") words)
        words (flatten words)
        words (set (filter #(not (empty? %)) words))]
    words))

(defn- text-word-score
  [text word]
  (if (substring? word text) 1.0 0.0))

(defn text-score
  [hg text sym]
  (let [words (symbol->words hg sym)]
    (apply + (map #(text-word-score text %) words))))

(defn guess
  [hg name text]
  (if (not (symb/root? name))
    name
    (let [text (clojure.string/lower-case text)
          can-mean (ops/symbols-with-root hg name)]
      (if (empty? can-mean)
        (symb/new-meaning (symb/str->symbol name))
        (let [scored (map #(hash-map
                            :symbol %
                            :score (text-score hg text %))
                          can-mean)
              max-score (apply max (map :score scored))
              high-scores (filter #(>= (:score %) max-score) scored)]
          ;; TODO: in case of tie, return highest degree
          ;;(apply max-key :degree high-scores))
          (:symbol (first high-scores)))))))

