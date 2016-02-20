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

(ns graphbrain.gbui.search
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.id :as id])
  (:use [jayq.core :only [$]]))

(def initialised (atom false))

(def mode (atom :search))

(hiccups/defhtml search-dialog-template []
  [:div {:class "modal"
         :role "dialog"
         :aria-hidden "true"
         :id "search-results-modal"}
   [:div {:class "modal-dialog"}
     [:div {:class "modal-content"}
       [:div {:class "modal-header"}
        [:a {:class "close" :data-dismiss "modal"} "×"]
        [:h3 "Search Results"]
        [:div {:class "btn-group"
               :role "group"
               :aria-label "..."}
         [:button {:type "button"
                   :class "btn btn-default active"
                   :id "simple-button"}
          "Simple"]
         [:button {:type "button"
                   :class "btn btn-default"
                   :id "intersect-button"}
          "Intersect"]]]
      [:div {:class "modal-body" :id "search-results-body"}
       [:div {:class "modal-footer"}
        [:a {:class "btn btn-primary" :data-dismiss "modal"} "Close"]]]]]])

(defn simple-clicked!
  []
  (.addClass ($ "#simple-button") "active")
  (.removeClass ($ "#intersect-button") "active")
  (reset! mode :search))

(defn intersect-clicked!
  []
  (.removeClass ($ "#simple-button") "active")
  (.addClass ($ "#intersect-button") "active")
  (reset! mode :intersect))

(defn init-dialog!
  []
  (let [html (search-dialog-template)]
    (.appendTo ($ html) "body")
    (.modal ($ "#search-results-modal") "hide"))
  (jq/bind ($ "#simple-button") "click" simple-clicked!)
  (jq/bind ($ "#intersect-button") "click" intersect-clicked!)
  (simple-clicked!))

(defn show-dialog!
  []
  (if (not @initialised)
    (do (init-dialog!)
        (reset! initialised true)))
  (.modal ($ "#search-results-modal") "show"))

(defn- link
  [result mode]
  (case mode
    :search (str "/n/" (first result))
    :intersect (str "/x?id1=" (first result)
                    "&id2=" @g/root-id)
    :change "#"
    :define "#"
    "#"))

(defn link-id
  [id]
  (str "sl_"
       (id/clean id)))

(defn rendered-results
  [msg]
  (let [results (:results msg)
        mode (:mode msg)]
    (clojure.string/join
     (map #(str "<p><a href='#' id='"
                (link-id (first %))
                "'>"
                (second %)
                "</a></p>")
          results))))

(defn- on-click-result!
  [result]
  (set!
   (.-href js/window.location)
   (link result @mode)))

(defn results-received
  [msg]
  (let [results (:results msg)
        html (if (empty? results)
               "<p>Sorry, no results found.</p>"
               (str "<p>" (count results) " results found.</p>"
                    (rendered-results msg)))]
    (show-dialog!)
    (.html ($ "#search-results-body") html)
    (doseq [result results]
      (jq/bind
       ($ (str "#" (link-id (first result))))
       "click"
       #(on-click-result! result)))))

(defn request!
  [query mode f]
  (jq/ajax {:type "POST"
            :url "/search"
            :data (str "q=" (.toLowerCase query)
                       "&mode=" (name mode))
            :dataType "text"
            :success f}))
