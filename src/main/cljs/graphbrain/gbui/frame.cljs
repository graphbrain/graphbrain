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

(ns graphbrain.gbui.frame
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.item :as item])
  (:use [jayq.core :only [$]]))

(hiccups/defhtml frame-html
  [snode-id rel-text]
  [:div {:id snode-id :class "frame"}
   [:div {:class "frame-label"}
    rel-text ":"]
   [:div {:class "frame-inner"}]])

(defn place!
  [snode-pair ctxts]
  (let [snode-id (first snode-pair)
        snode (second snode-pair)
        relpos (:rpos snode)
        rel-text (:label snode)
        html (frame-html snode-id rel-text)]
    (jq/append ($ "#frames") html)
    (doseq [node (:nodes snode)]
      (item/item-place node snode-id snode ctxts))))
