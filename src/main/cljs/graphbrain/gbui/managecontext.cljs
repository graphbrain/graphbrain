(ns graphbrain.gbui.managecontext
  (:require-macros [hiccups.core :as hiccups])
  (:require [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.user :as user]
            [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$]]))

(def initialised (atom false))

(hiccups/defhtml create-manage-dialog-template
  []
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "manage-context-modal"}
   [:div {:class "modal-dialog"}
    [:div {:class "modal-content"}
     [:div {:class "modal-header"}
      [:a {:class "close" :data-dismiss "modal"} "×"]
      [:h3 "Manage GraphBrain"]]
     [:form {:id "manage-context-form"
             :action "/manage-context"
             :method "post"}
      [:div {:class "modal-body" :id "manage-context-body"}
       [:span {:id "manage-context-error-message" :class "text-danger"}]
       [:div {:id "name-formgroup" :class "form-group"}
        [:label {:class "control-label"} "Name"]
        [:input {:id "su-name"
                 :name "name"
                 :type "text"
                 :class "form-control input-sm"
                 :placeholder "Short name"}]]
       [:div {:id "desc-formgroup" :class "form-group"}
        [:label {:class "control-label"} "Description"]
        [:input {:id "su-desc"
                 :name "desc"
                 :type "text"
                 :class "form-control input-sm"
                 :placeholder "What is is about?"}]]]
      [:div {:class "modal-footer"}
       [:button
        {:type "submit" :class "btn btn-primary"} "Create"]]]]]])

(defn init-dialog!
  []
  (.appendTo
   ($ (create-manage-dialog-template)) "body"))

(defn show-manage-context-dialog!
  []
  (if (not @initialised)
    (do
      (init-dialog!)
      (reset! initialised true)))
  (.modal ($ "#manage-context-modal") "show"))

(defn request!
  []
  (show-manage-context-dialog!))
