(ns graphbrain.web.css.node
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]]))

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

  [:.node_root
   {:color "#000"
    :background "#eee"
    :margin (px 3)
    :padding (px 10)}]

  [:.node-main
   {:float "left"
    :max-width (px 160)}]

  [:.nodeTitle
   {:font-size (px 14)
    :float "left"}]

  [:.nodeTitle
   [:a
    {:color "#000"
     :float "left"}]]

  [:.nodeTitle_root
   {:max-width (px 600)}]

  [:.nodeTitle_root
   [:a
    {:color "#000"
     :line-height "110%"}]]

  [:.nodeSubText
   {:float "left"
    :font-size (px 11)
    :color "#555"}]

  [:.nodeSubText
   [:a
    {:font-size (px 11)
     :color "#555"}]]

  [:.nodeRemove
   {:text-align "right"
    :float "right"
    :margin-left (px 10)}]

  [:.nodeRemove
   [:a
    {:color "#B0B0B0"}]]

  [:.nodeUrl
   {:overflow "hidden"
    :text-overflow "ellipsis"
    :max-width (px 170)
    :float "left"}]

  [:.nodeUrl
   [:a
    {:color "rgb(0, 0, 255)"}]]

  [:.nodeUrl_root
   {:font-size (px 16)
    :overflow "hidden"
    :text-overflow "ellipsis"
    :max-width (px 600)}]

  [:.nodeUrl_root
   [:a
    {:color "rgb(0, 0, 255)"}]]

  [:.nodeIco
   {:margin-right (px 5)
    :float "left"}]])
