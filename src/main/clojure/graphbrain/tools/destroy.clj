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

(ns graphbrain.tools.destroy
  (:require [graphbrain.hg.ops :as ops]
            [clojure.term.colors :refer :all]))

(defn do-it!
  [hg]
  (println (red "WARNING: This will erase the hypergraph."))
  (print (bold "Are you sure you want to continue [yes/NO]? "))
  (flush)
  (let [input (read-line)]
    (if (= input "yes")
      (do
        (println "Deleting the hypergraph.")
        (ops/destroy! hg)
        (println "done."))
      (println "Aborting."))))
