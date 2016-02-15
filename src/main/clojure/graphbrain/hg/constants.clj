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

(ns graphbrain.hg.constants)

;; Hypergraph
(def ^:const degree "deg/1")
(def ^:const index "ind/1")
(def ^:const source "src/1")

;; Relations
(def ^:const type-of "type_of/1")
(def ^:const synonym "synonym/1")
(def ^:const part-of "part_of/1")
(def ^:const antonym "antonym/1")
(def ^:const related "related/1")
(def ^:const part-of-speech "pos/1")

;; Parts-of-speech
(def ^:const noun "noun/1")
(def ^:const verb "verb/1")
(def ^:const adjective "adjective/1")
(def ^:const adverb "adverb/1")

;; WordNet
(def ^:const wordnet "wordnet/1")

;; Logo
(def ^:const ascii-logo
  " __   __        __        __   __              
/ _` |__)  /\\  |__) |__| |__) |__)  /\\  | |\\ | 
\\__> |  \\ /~~\\ |    |  | |__) |  \\ /~~\\ | | \\| ")
