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

(ns graphbrain.web.visualvert
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]))

(declare edge-id->text)

(defn id->label
  [id ctxt]
  #_(case (id/id->type id)
    :entity (entity/label id)
    :edge (edge-id->text id ctxt)
    :context (context/label id)
    :user (entity/label id)
    :edge-type (entity/text id)
    :url (url/id->url id)
    id))

(defn id->html
  [id ctxt]
  #_(if (= (id/id->type id) :edge-type)
    (id->label id ctxt)
    (str "<a href='/n/" (:id ctxt) "/" id "'>"
         (id->label id ctxt)
         "</a>")))

(defn edge-id->text
  [edge-id ctxt]
  #_(let [labels (map #(id->html (id/eid->id %) ctxt)
                    (id/id->ids edge-id))]
    (str (second labels)
         " "
         (first labels)
         " "
         (clojure.string/join " "
                              (rest
                               (rest labels))))))

(defn id->visual
  [hg id ctxt ctxts]
  #_(let [vtype (id/id->type id)
        vert (maps/id->vertex id)
        vert (maps/local->global vert)]
    (case (:type vert)
      :entity (let [sub (entity/subentities vert)
                    sub (map #(hash-map :id (id/eid->id %)
                                        :text (entity/description %)) sub)]
                (assoc vert
                  :sub sub
                  :text (entity/label id)))
      :url (let [url (url/url vert)
                 title (url/title gbdb id ctxts)
                 title (if (empty? title) url title)
                 icon (str "http://www.google.com/s2/favicons?domain=" url)]
             (assoc vert
               :text title
               :url url
               :icon icon))
      :user (let [u (db/getv gbdb id ctxts)]
              {:id id
               :type :user
               :text (:name u)
               :sub [{:id "#" :text "GraphBrain user"}]})
      :text (text/id->text gbdb id)
      :context {:id id
                :type :context
                :text (context/label id)
                :sub [{:id "#" :text "a GraphBrain"}]}
      :edge {:id id
             :type :edge
             :text (edge-id->text id ctxt)}
      (assoc vert
        :text id))))
