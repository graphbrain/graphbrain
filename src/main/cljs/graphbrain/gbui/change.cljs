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
         [:form {:id "change-form" :action (str "/node/" root-node-id) :method "post"}
          [:input {:type "hidden" :name "op" :value "remove"}]
          [:input {:id "removeEdgeField" :type "hidden" :name "edge"}]
          [:div {:class "modal-body"}
           [:p {:id "link-desc"}]
           [:div {:id "alt-entities"}]]
          [:div {:class "modal-footer"}
           [:a {:class "btn" :data-dismiss "modal"} "Close"]
           [:a {:id "new-meaning-button" :class "btn btn-warning"} "New Meaning"]
           [:a {:id "remove-button" :class "btn btn-danger"} "Remove"]]]]]]])

(defn change-action
  []
  (.submit ($ "#change-form")))

(defn init-dialog
  [root-node-id]
  (let [html (change-dialog-template root-node-id)]
    (jq/append ($ "body") html)
    (jq/bind ($ "#remove-button") :click change-action)))

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
  (let [edge (:edge node)
        link (:label snode)
        html (str (:text node) " <strong>(" link ")</strong>")]
    (jq/val ($ "#removeEdgeField") edge)
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
