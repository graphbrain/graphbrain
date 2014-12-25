(ns graphbrain.gbui.change
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.search :as search])
  (:use [jayq.core :only [$]]))

(hiccups/defhtml change-dialog-template
  [root-node-id]
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "change-modal"}
   [:div {:class "modal-dialog"}
    [:div {:class "modal-content"}
     [:div {:class "modal-header"}
      [:a {:class "close" :data-dismiss "modal"} "×"]
      [:h3 "Change"]
      [:form {:id "change-form" :action (str "/n/" root-node-id) :method "post"}
       [:input {:id "op-field" :type "hidden" :name "op"}]
       [:input {:id "eid-field" :type "hidden" :name "eid"}]
       [:input {:id "edge-field" :type "hidden" :name "edge"}]
       [:input {:id "score-field" :type "hidden" :name "score"}]       
       [:div {:class "modal-body"}
        [:p {:id "link-desc"}]
        [:div {:id "alt-entities"}]]
       [:div {:class "modal-footer"}
        [:a {:class "btn" :data-dismiss "modal"} "Close"]
        [:a {:id "new-meaning-button" :class "btn btn-warning"} "New Meaning"]
        [:a {:id "remove-button" :class "btn btn-danger"} "Remove"]]]]]]])

(defn- submit-remove
  []
  (jq/val ($ "#op-field") "remove")
  (.submit ($ "#change-form")))

(defn- submit-new-meaning
  []
  (jq/val ($ "#op-field") "new-meaning")
  (.submit ($ "#change-form")))

(defn init-dialog
  [root-node-id]
  (let [html (change-dialog-template root-node-id)]
    (jq/append ($ "body") html)
    (jq/bind ($ "#remove-button") :click submit-remove)
    (jq/bind ($ "#new-meaning-button") :click submit-new-meaning)))

(defn- on-changed
  []
  (.reload js/window.location))

(defn- change-request!
  [node new-id]
  (jq/ajax {:type "POST"
            :url "/change"
            :data (str "edge=" (js/encodeURIComponent (:edge node))
                       "&old-id=" (js/encodeURIComponent (:id node))
                       "&new-id=" (js/encodeURIComponent new-id))
            :dataType "text"
            :success on-changed}))

(defn- on-change
  [node new-id]
  (change-request! node new-id))

(defn show-change-dialog
  [msg node snode]
  (let [eid (:eid node)
        edge (:edge node)
        score (:score node)
        link (:label snode)
        html (str (:text node) " <strong>(" link ")</strong>")]
    (jq/val ($ "#eid-field") eid)
    (jq/val ($ "#edge-field") edge)
    (jq/val ($ "#score-field") score)
    (jq/html ($ "#link-desc") html)
    (jq/html ($ "#alt-entities")
             (search/rendered-results msg))
    (let [results (:results msg)]
      (doseq [result results]
        (jq/bind
         ($ (str "#" (search/link-id (first result))))
         "click"
         #(on-change node (first result)))))
    (.modal ($ "#change-modal") "show")))

(defn- results-received
  [msg node snode]
  (show-change-dialog (cljs.reader/read-string msg)
                      node
                      snode))

(defn clicked
  [node snode]
  (search/request! (:text node)
                   :change
                   #(results-received % node snode)))
