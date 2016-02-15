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

(ns graphbrain.web.extrasnodes
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.web.common :as common]
            [graphbrain.web.visualvert :as vv]))

(defn- edge->map
  [edge ctxt]
  (let [vv (vv/id->visual nil (:id edge) ctxt nil)]
    (assoc vv
      :edge (:id edge)
      :edge-text (:text vv))))

(defn- entity->map
  [ent ctxt pre-label]
  (let [vv (vv/id->visual nil (:eid ent) ctxt nil)]
    (assoc vv
      :edge (:id ent)
      :edge-text (str pre-label (:text vv)))))

(defn- url->map
  [url ctxt pre-label]
  (let [vv (vv/id->visual nil (:id url) ctxt nil)]
    (assoc vv
      :edge (:id url)
      :edge-text (str pre-label (:text vv)))))

(defn- popular-entities
  [ctxt]
  #_(let [ents (gb/popular-n-entities common/gbdb (:id ctxt) 10)]
    {:nodes (map #(entity->map % ctxt "Popular entity: ") ents)
     :label "Popular entities"
     :static true}))

(defn- popular-urls
  [ctxt]
  #_(let [ents (gb/popular-n-urls common/gbdb (:id ctxt) 10)]
    {:nodes (map #(url->map % ctxt "Popular url: ") ents)
     :label "Popular urls"
     :static true}))

(defn- recent-entities
  [ctxt]
  #_(let [ents (gb/recent-n-entities common/gbdb (:id ctxt) 10)]
    {:nodes (map #(entity->map % ctxt "Recent entity: ") ents)
     :label "Recent entities"
     :static true}))

(defn- recent-urls
  [ctxt]
  #_(let [ents (gb/recent-n-urls common/gbdb (:id ctxt) 10)]
    {:nodes (map #(url->map % ctxt "Recent url: ") ents)
     :label "Recent urls"
     :static true}))

(defn- recent-edges
  [ctxt]
  #_(let [edges (gb/recent-n-edges common/gbdb (:id ctxt) 10)]
    {:nodes (map #(edge->map % ctxt) edges)
     :label "Recent additions"
     :static true}))

(defn extrasnodes
  [hg root-id ctxt ctxts snodes]
  (if (= root-id (:id ctxt))
    (assoc snodes
      "recent" (recent-edges ctxt)
      "popular_entities" (popular-entities ctxt)
      "popular_urls" (popular-urls ctxt)
      "recent_entities" (recent-entities ctxt)
      "recent_urls" (recent-urls ctxt))
    snodes))
