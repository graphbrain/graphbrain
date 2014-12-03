(ns graphbrain.web.views.eco
  (:use hiccup.core)
  (:require [graphbrain.web.views.barpage :as bar]))

(defn sentence
  [words]
  (interpose " "
             (map #(vector :span {:class "eco-word"}
                           (:word %)
                           "/"
                           [:span {:class "eco-pos"} (:pos %)]
                           "/"
                           [:span {:class "eco-lemma"} (:lemma %)]) words)))

(defn trace
  [rs]
  (map #(vector :div
                [:span {:class "eco-weight"} (:weight %)]
                " "
                [:span {:class "eco-rule"} (:desc (:rule %))]
                " "
                [:span {:class "eco-vertex"} (:vert %)]
                [:div {:class "eco-trace-box"}
                 (let [v (:vertex %)]
                   (if (coll? v)
                     (trace v) v))])
       (sort-by #(if (:weight %)
                   (:weight %) 0) > rs)))

(defn view
  [report]
  (html
   [:div {:id "eco-results"}
    [:div {:class "eco-section"} (sentence (:words report))]
    [:div {:class "eco-section"} (:res report)]
    [:div {:class "eco-section"} (:id (:edge report))]
    [:div {:class "eco-section"} (trace (:vws report))]]))

(defn page
  [& {:keys [title css-and-js user js report]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :js js
               :content-fun #(view report)))
