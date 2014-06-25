(ns graphbrain.gbui.remove
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$]]))

(hiccups/defhtml remove-dialog-template [root-node-id]
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "removeModal"}
    [:div {:class "modal-dialog"}
      [:div {:class "modal-content"}
        [:div {:class "modal-header"}
          [:a {:class "close" :data-dismiss "modal"} "×"]
          [:h3 "Confirm Removal"]
        [:form {:id "removeForm" :action (str "/node/" root-node-id) :method "post"}
          [:input {:type "hidden" :name "op" :value "remove"}]
          [:input {:id "removeEdgeField" :type "hidden" :name "edge"}]
          [:div {:class "modal-body" :id "addBrainBody"}
            [:div {:id "linkDesc"}]]
          [:div {:class "modal-footer"}
            [:a {:class "btn" :data-dismiss "modal"} "Close"]
            [:a {:id "removeDlgButton" :class "btn btn-primary"} "Remove"]]]]]]])

(defn remove-action
  []
  (.submit ($ "#removeForm")))

(defn init-remove-dialog
  [root-node-id]
  (let [html (remove-dialog-template root-node-id)]
    (jq/append ($ "body") html)
    (jq/bind ($ "#removeDlgButton") :click remove-action)))

(defn show-remove-dialog
  [node snode]
  (let [edge (:edge node)
        link (:label snode)
        html (str (:text node) " <strong>(" link "</strong>)")]
    (jq/val ($ "#removeEdgeField") edge)
    (jq/html ($ "#linkDesc") html)
    (.modal ($ "#removeModal") "show")))

(defn remove-clicked
  [node snode]
  (show-remove-dialog node snode))
