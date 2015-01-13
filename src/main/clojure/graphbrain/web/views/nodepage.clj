(ns graphbrain.web.views.nodepage
  (:use hiccup.core)
  (:require [graphbrain.db.entity :as entity]
            [graphbrain.web.views.barpage :as bar]))

(defn- subid->link
  [id]
  [:a {:href (str "/x/" id)}
   (entity/label id)])

(defn- manage-context
  [ctxt]
  [:div {:id "manage-context"}
   [:form {:class "form-inline"
           :action "/grant-perm"
           :method "post"}
    [:input {:name "ctxt"
             :type "hidden"
             :value (:id ctxt)}]
    [:div {:class "form-group manage-form-elem"}
     [:input {:type "text"
              :class "form-control"
              :placeholder "Email or username"
              :name "email-username"}]]
    [:div {:class "form-group manage-form-elem"}
     [:select {:class "form-control"
               :name "role"}
      [:option "Editor"]
      [:option "Administrator"]]]
    [:button {:type "submit"
              :class "btn btn-primary manage-form-elem"}
     "Add Collaborator"]]])

(defn view
  [title desc show-manage-form ctxt]
  (html
   [:div {:id "nodepage"}
    [:div {:id "nodepage-title"}
     [:div {:class "np-title"}
      title]
     [:div {:class "np-desc"}
      (if (string? desc)
        desc
        (interpose ", "
                   (map subid->link desc)))]]

    (if show-manage-form
      (manage-context ctxt))
    
    [:div {:id "frames"}]]))

(defn nodepage
  [& {:keys [title css-and-js user ctxt js desc show-manage-form]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :ctxt ctxt
               :js js
               :content-fun #(view title desc show-manage-form ctxt)))
