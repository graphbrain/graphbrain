(ns graphbrain.web.css.item
  (:require [garden.units :refer [px]]))

(def css
  [[:.item
   {:color "#000"
    :margin (px 3)
    :padding (px 3)
    :clear "both"}]

  [:.item
   [:a
    {:color "#000"}]]

  [:.item-main
   {:float "left"
    :max-width (px 560)}]

  [:.item-title
   {:font-size (px 15)
    :float "left"}]

  [:.item-title
   [:a
    {:color "#FFF"
     :float "left"}]]

  [:.item-url-title
   {:float "left"
    :overflow "hidden"
    :text-overflow "ellipsis"
    :white-space "nowrap"}]

  [:.item-url-title
   [:a
    {:font-size (px 12)
     :color "#000"
     :max-width (px 165)
     :float "left"}]]
  
  [:.node-title-root
   {:max-width (px 600)}]

  [:.item-sub-text
   {:float "left"
    :font-size (px 11)
    :color "#555"}]

  [:.item-sub-text
   [:a
    {:font-size (px 11)
     :color "#555"}]]

  [:.item-remove
   {:text-align "right"
    :float "right"
    :margin-left (px 10)}]

  [:.item-remove
   [:a
    {:color "#B0B0B0"}]]

  [:.item-url
   {:max-width (px 100)
    :background "rgb(255, 0, 0)"
    :overflow "hidden"
    :text-overflow "ellipsis"
    :white-space "nowrap"
    :float "right"}]
  
  [:.item-url
   [:a
    {:font-size (px 10)
     :color "rgb(0, 0, 255)"}]]

  [:.item-ico
   {:margin-right (px 5)
    :float "left"}]

  [:.item-ico
   [:img
    {:margin-right (px 5)
     :float "left"}]]])
