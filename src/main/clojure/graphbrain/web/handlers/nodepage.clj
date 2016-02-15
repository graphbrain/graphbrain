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

(ns graphbrain.web.handlers.nodepage
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.web.common :as common]
            [graphbrain.web.snodes :as snodes]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.nodepage :as np]
            [graphbrain.web.encoder :as enc]))

(defn- data
  [id user ctxt ctxts]
  (let [snodes (snodes/generate common/hg id ctxt ctxts)]
    {:root-id id
     :snodes snodes}))

(defn- js
  [id user ctxt ctxts]
  (str "var ptype='node';"
       "var data='" (enc/encode (pr-str
                      (data id user ctxt ctxts))) "';"))

(defn handle
  [request]
  #_(let
      [user (common/get-user request)
       id (:* (:route-params request))
       ctxts (contexts/active-ctxts id user)
       vert (gb/getv common/gbdb
                     id
                     ctxts)
       title (case (:type vert)
               :url (url/title common/gbdb (:id vert) ctxts)
               (vertex/label vert))
       desc (case (:type vert)
              :entity (entity/subentities vert)
              :url "web page"
              :user "GraphBrain user"
              nil)
       ctxt (contexts/context-data id (:id user))]
    (common/log request (str "nodepage: " id))
    (np/nodepage :title title
                 :css-and-js (css+js/css+js)
                 :user user
                 :ctxt ctxt
                 :js (js id user ctxt ctxts)
                 :desc desc)))
