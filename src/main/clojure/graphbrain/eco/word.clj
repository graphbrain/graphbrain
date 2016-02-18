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

(ns graphbrain.eco.word
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
