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

(ns graphbrain.web.handlers.eco
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.disambig.edgeguesser :as edg]
            [graphbrain.eco.eco :as eco]
            [graphbrain.eco.words :as words]
            [graphbrain.eco.parsers.chat :as chat]
            [graphbrain.web.views.eco :as ecop]))

(defn- js
  []
  (str "var ptype='eco';"))

(defn- sentence->result
  [user sentence ctxts]
  #_(let
      [env {:root "561852ced99a782d/europe"
            :user (:id user)}
       words (words/str->words sentence)
       par (eco/parse chat/chat words env)
       vws (map eco/vert+weight par)
       res (eco/verts+weights->vertex chat/chat vws env)]
    (if (id/edge? res)
      (let [edge-id (edg/guess common/gbdb res sentence (:id user) ctxts)
            edge (maps/id->vertex edge-id)
            edge (assoc edge :score 1)]
        {:words words
         :res res
         :vws vws
         :edge edge})
      {:words words
       :res res
       :vws vws})))

(defn report
  [user sentence ctxts]
  (sentence->result user sentence ctxts))

(defn handle
  [request]
  (let
      [sentence ((request :form-params) "input-field")
       title "Eco test"
       report (if sentence
                (report sentence))]
    (ecop/page :title title
               :js (js)
               :report report
               :dev (:dev request))))
