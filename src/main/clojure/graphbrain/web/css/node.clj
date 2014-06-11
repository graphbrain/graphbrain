(ns graphbrain.web.css.node
  (:require [garden.units :refer [px]]))

(def css
  [[:.node
   {:color "#000"
    :background "rgb(230, 230, 230)"
    :margin (px 3)
    :padding (px 3)
    :clear "both"}]

  [:.node
   [:a
    {:color "#000"}]]

  [:.node-root
   {:color "#000"
    :background "#eee"
    :margin (px 3)
    :padding (px 10)}]

  [:.node-main
   {:float "left"
    :max-width (px 160)}]

  [:.node-title
   {:font-size (px 13)
    :float "left"}]

  [:.node-title
   [:a
    {:color "#000"
     :float "left"}]]

  [:.node-url-title
   {:float "left"
    :overflow "hidden"
    :text-overflow "ellipsis"
    :white-space "nowrap"}]

  [:.node-url-title
   [:a
    {:font-size (px 12)
     :color "#000"
     :max-width (px 165)
     :float "left"}]]
  
  [:.node-title-root
   {:max-width (px 600)}]

  [:.node-title-root
   [:a
    {:color "#000"
     :line-height "110%"}]]

  [:.node-sub-text
   {:float "left"
    :font-size (px 11)
    :color "#555"}]

  [:.node-sub-text
   [:a
    {:font-size (px 11)
     :color "#555"}]]

  [:.node-remove
   {:text-align "right"
    :float "right"
    :margin-left (px 10)}]

  [:.node-remove
   [:a
    {:color "#B0B0B0"}]]

  [:.node-url
   {:overflow "hidden"
    :text-overflow "ellipsis"
    :max-width (px 135)
    :white-space "nowrap"
    :float "right"}]

  [:.node-url
   [:a
    {:font-size (px 10)
     :color "rgb(0, 0, 255)"}]]

  [:.node-url-root
   {:font-size (px 16)
    :overflow "hidden"
    :text-overflow "ellipsis"
    :max-width (px 600)}]

  [:.node-url-root
   [:a
    {:color "rgb(0, 0, 255)"}]]

  [:.node-ico
   {:margin-right (px 5)
    :float "left"}]

  [:.node-ico
   [:img
    {:margin-right (px 5)
     :float "left"}]]])
