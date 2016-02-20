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
            [graphbrain.hg.symbol :as symb]
            [graphbrain.disambig.edgeguesser :as edg]
            [graphbrain.eco.eco :as eco]
            [graphbrain.eco.words :as words]
            [graphbrain.eco.parsers.chat :as chat]
            [graphbrain.web.views.eco :as ecop]))

(defn- js
  []
  (str "var ptype='eco';"))

(defn- sentence->result
  [hg sentence]
  (let
      [env {:root "561852ced99a782d/europe"
            :user "eco/1"}
       words (words/str->words sentence)
       par (eco/parse chat/chat words env)
       vws (map eco/vert+weight par)
       res (eco/verts+weights->vertex chat/chat vws env)]
    (if (= (symb/sym-type res) :edge)
      (let [edge (edg/guess hg res sentence)]
        {:words words
         :res res
         :vws vws
         :edge edge})
      {:words words
       :res res
       :vws vws})))

(defn handle
  [request hg]
  (let [sentence ((request :form-params) "input-field")
        title "Eco test"
        report (if sentence (sentence->result hg sentence))]
    (ecop/page :title title
               :js (js)
               :report report
               :dev (:dev request))))
