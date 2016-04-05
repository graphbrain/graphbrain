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

(ns graphbrain.repl
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.beliefs :as beliefs]
            [graphbrain.hg.symbol :as symb]
            [graphbrain.hg.constants :as const]
            [graphbrain.hg.connection :as conn]
            [clojure.term.colors :refer :all]
            [clojure.string :as string]))

(defn init!
  ([hg]
     (ns graphbrain.repl)
     (def hg hg))
  ([]
     (println (cyan const/ascii-logo))
     (println)
     (def hg (conn/create :mysql "gb"))))

(defn ?
  [query]
  (println
   (ops/symbols-with-root hg
                          (symb/str->symbol
                           (string/join "_"
                                     (string/split query #" "))))))

(defn edges
  [symbol]
  (println
   (ops/star hg symbol)))

(defn edges-srcs
  [symbol]
  (doseq [edge (ops/star hg symbol)]
    (println (str edge " {" (beliefs/sources hg edge)  "}"))))


(defn degree
  [symbol]
  (println
   (ops/degree hg symbol)))

(defn sources
  [edge]
  (beliefs/sources hg edge))
