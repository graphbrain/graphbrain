(ns graphbrain.gbui.change
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$]]))

(hiccups/defhtml change-dialog-template
  [root-node-id]
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "change-modal"}
   [:div {:class "modal-dialog"}
     [:div {:class "modal-content"}
       [:div {:class "modal-header"}
         [:a {:class "close" :data-dismiss "modal"} "×"]
         [:h3 "Change"]
         [:form {:id "change-form" :action (str "/node/" root-node-id) :method "post"}
          [:input {:type "hidden" :name "op" :value "remove"}]
          [:input {:id "removeEdgeField" :type "hidden" :name "edge"}]
          [:div {:class "modal-body" :id "addBrainBody"}
           [:div {:id "linkDesc"}]]
          [:div {:class "modal-footer"}
           [:a {:class "btn" :data-dismiss "modal"} "Close"]
           [:a {:id "removeDlgButton" :class "btn btn-primary"} "Remove"]]]]]]])

(defn change-action
  []
  (.submit ($ "#change-form")))

(defn init-dialog
  [root-node-id]
  (let [html (change-dialog-template root-node-id)]
    (jq/append ($ "body") html)
    (jq/bind ($ "#removeDlgButton") :click change-action)))

(defn show-change-dialog
  [node snode]
  (let [edge (:edge node)
        link (:label snode)
        html (str (:text node) " <strong>(" link ")</strong>")]
    (jq/val ($ "#removeEdgeField") edge)
    (jq/html ($ "#linkDesc") html)
    (.modal ($ "#change-modal") "show")))

(defn clicked
  [node snode]
  (show-change-dialog node snode))
