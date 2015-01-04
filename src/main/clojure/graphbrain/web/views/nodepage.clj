(ns graphbrain.web.views.nodepage
  (:use hiccup.core)
  (:require [graphbrain.db.entity :as entity]
            [graphbrain.web.views.barpage :as bar]))

(defn- subid->link
  [id]
  [:a {:href (str "/x/" id)}
   (entity/label id)])

(defn view
  [title desc]
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
    [:div {:id "frames"}]]))

(defn nodepage
  [& {:keys [title css-and-js user ctxt js desc]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :ctxt ctxt
               :js js
               :content-fun #(view title desc)))
