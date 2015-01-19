(ns graphbrain.web.css.item
  (:require [garden.units :refer [px]]))

(def css
  [[:.clearfix:after
   {:content " "
    :display "block" 
    :height 0 
    :clear "both"}]

   [:.item
    {:color "#000"
     :margin (px 3)
     :padding (px 3)
     :position "relative"}]

   [:.item
    [:a
     {:color "#000"}]]

   [:.item:hover
    {:background "#EEE"}]
   
   [:.item-main
    {:margin-right (px 15)}]

   [:.item-title
    {:font-size (px 15)
     :padding (px 3)
     :color "#0F52BA"}]

   [:.item-title
    [:a
     {:color "#333"}]]

   [:.item-sub-text
    {:font-size (px 11)
     :margin-left (px 5)
     :color "#555"}]
  
   [:.item-sub-text
    [:a
     {:font-size (px 11)
      :color "#555"}]]
   
   [:.item-url-title
    {:overflow "hidden"
     :text-overflow "ellipsis"
     :white-space "nowrap"}]

   [:.item-url-title
    [:a
     {:font-size (px 12)
      :color "#333"
      :padding (px 3)}]]

   [:.item-url-area {}]
  
   [:.item-url
    {:overflow "hidden"
     :white-space "nowrap"
     :text-overflow "ellipsis"}]
  
   [:.item-url
    [:a
     {:font-size (px 10)
      :color "rgb(0, 0, 255)"
      :overflow "hidden"
      :white-space "nowrap"
      :text-overflow "ellipsis"}]]

   [:.item-ico
    {:float "left"
     :margin-top (px 2)
     :margin-right (px 3)}]

   [:.item-ico
    [:img
     {:float "left"
      :vertical-align "middle"}]]

   [:.item-remove
    {:position "absolute"
     :top 0
     :right 0
     :width (px 15)
     :padding-right (px 3)
     :text-align "right"}]

   [:.item-remove
    [:a
     {:color "#B0B0B0"}]]])
