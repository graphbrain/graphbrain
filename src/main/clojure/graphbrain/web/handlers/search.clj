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

(ns graphbrain.web.handlers.search
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.symbol :as symb]
            [graphbrain.nlp.summaries :as summ]))

(defn query
  [hg q]
  (map #(list % (str
                 (symb/symbol->str
                  (symb/root %))
                 " (" (summ/label hg %) ")"))
       (ops/symbols-with-root hg (symb/str->symbol q))))

(defn reply
  [results mode]
  (pr-str {:type :search
           :mode mode
           :results results}))

(defn handle
  [request hg]
  (let [q ((request :form-params) "q")
        mode (keyword ((request :form-params) "mode"))]
    (reply (query hg q) mode)))
