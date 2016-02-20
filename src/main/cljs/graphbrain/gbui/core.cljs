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

(ns graphbrain.gbui.gbui
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.animation :as anim]
            [graphbrain.gbui.inters :as inters]
            [graphbrain.gbui.nodepage :as nodepage]
            [graphbrain.gbui.eco :as eco]
            [graphbrain.gbui.encoder :as enc]
            [graphbrain.gbui.input :as input]
            [cemerick.pprng :as rng])
  (:use [jayq.core :only [$]]))

(defn- init-data
  [data-str]
  (let [data (cljs.reader/read-string
              (enc/decode data-str))]
    (reset! g/data data)))

(defn init-interface
  []
  (jq/bind ($ "#top-input-field") "submit" input/query))

(defn start
  []
  (reset! g/rng (rng/rng "GraphBrain GraphBrain"))

  (if (some #{js/ptype} ["node" "intersect" "brain"])
    (init-data js/data))
  
  (case js/ptype
    "node" (nodepage/init-nodepage!)
    "intersect" (do (inters/init-view!)
                    (anim/add-anim! (anim/anim-graph-layout)))
    "eco" (eco/init-eco!)
    nil)
  
  (init-interface))

($ start)
