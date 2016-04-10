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

(ns graphbrain.nlp.summaries
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.symbol :as symb]
            [graphbrain.hg.constants :as const]))

(defn label
  [hg symbol]
  (let [types (ops/pattern->edges hg [const/type-of symbol nil])]
    (if (empty? types)
      "?"
      (clojure.string/join ", "
                           (map #(symb/symbol->str
                                  (symb/root (% 2))) types)))))
