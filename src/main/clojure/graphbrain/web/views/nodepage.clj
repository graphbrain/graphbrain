(ns graphbrain.web.views.nodepage
  (:use hiccup.core)
  (:require [graphbrain.db.entity :as entity]
            [graphbrain.web.views.barpage :as bar]))

(defn- subid->link
  [id]
  [:a {:href (str "/x/" id)}
   (entity/label id)])

(defn- admin-context
  [ctxt]
  [:div {:id "admin-context"
         :class "after-title-frame"}
   [:form {:class "form-inline"
           :action "/grant-perm"
           :method "post"}
    [:input {:name "ctxt"
             :type "hidden"
             :value (:id ctxt)}]
    [:div {:class "form-group after-title-form-elem"}
     [:input {:type "text"
              :class "form-control"
              :placeholder "Email or username"
              :name "email-username"}]]
    [:div {:class "form-group after-title-form-elem"}
     [:select {:class "form-control"
               :name "role"}
      [:option "Editor"]
      [:option "Administrator"]]]
    [:button {:type "submit"
              :class "btn btn-primary after-title-form-elem"}
     "Add Collaborator"]]])

(defn- follow-unfollow
  [ctxt]
  [:div {:id "follow-unfollow"
         :class "after-title-frame"}
   [:form {:class "form-inline"
           :action "/follow-unfollow"
           :method "post"}
    [:input {:name "ctxt"
             :type "hidden"
             :value (:id ctxt)}]
    (if (:following ctxt)
      [:button {:type "submit"
                :class "btn btn-primary after-title-form-elem"}
       (str "Unfollow " (:name ctxt))]
      [:button {:type "submit"
                :class "btn btn-primary admin-form-elem"}
       (str "Follow " (:name ctxt))])]])

(defn view
  [title desc ctxt]
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

    (if (:admin ctxt)
      (admin-context ctxt))

    (if (:follow-unfollow ctxt)
      (follow-unfollow ctxt))
    
    [:div {:id "frames"}]]))

(defn nodepage
  [& {:keys [title css-and-js user ctxt js desc]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :ctxt ctxt
               :js js
               :content-fun #(view title desc ctxt)))
