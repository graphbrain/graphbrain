(ns graphbrain.web.views.eco
  (:use hiccup.core)
  (:require [graphbrain.web.views.barpage :as bar]))

(defn sentence
  [words]
  (map #(vector :span {:class "eco-word"}
                (:word %)
                "/"
                [:span {:class "eco-pos"} (:pos %)]
                "/"
                [:span {:class "eco-lemma"} (:lemma %)]) words))

(defn view
  [report]
  (html
   [:div {:id "eco-results"}
    [:div {:class "eco-section"} (sentence (:words report))]
    [:div {:class "eco-section"} (:res report)]
    [:div {:class "eco-section"} (:id (:edge report))]]))

(defn page
  [& {:keys [title css-and-js user js report]}]
  (bar/barpage :title title
               :css-and-js css-and-js
               :user user
               :js js
               :content-fun #(view report)))
