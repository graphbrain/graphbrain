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

(ns graphbrain.disambig.edgeguesser
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.disambig.entityguesser :as eg]))

(defn eid->guess-eid
  [hg eid text ctxt ctxts]
  #_(let [ids (id/id->ids eid)
        guess (eg/guess-eid gbdb (second ids) text nil ctxt ctxts)]
    (if (id/eid? guess) guess
        (id/name+ids->eid
         (first ids)
         (second ids)
         (map #(eg/guess-eid gbdb % text nil ctxt ctxts) (drop 2 ids))))))

(defn guess
  [hg id text ctxt ctxts]
  #_(case (id/id->type id)
    :entity (eg/guess-eid gbdb id text nil ctxt ctxts)
    :text (:id (gbdb/putv!
                gbdb
                (text/pseudo->vertex id)
                ctxt))
    :edge (if (id/eid? id)
            (eid->guess-eid gbdb id text ctxt ctxts)
            (id/ids->id (map #(guess gbdb % text ctxt ctxts) (id/id->ids id))))
    id))
