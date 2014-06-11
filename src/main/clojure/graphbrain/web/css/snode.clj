(ns graphbrain.web.css.snode
  (:require [garden.units :refer [px]]))

(def css
  [[:.snode
   {:max-width (px 200)
    :position "fixed"
    :font-size (px 12)
    :border-style "solid"
    :border-width (px 3)
    :border-color "#AAA"
    :padding (px 0)
    :background "rgba(255, 255, 255, 0.8)"}]

  [:.snodeR
   {:max-width "600px"
    :position "fixed"
    :font-size (px 28)
    :background "rgba(255, 255, 255, 0.8)"
    :border-width (px 0)
    :border-color "rgb(255, 255, 255)"
    :padding (px 0)}]

  [:.snodeLabel
   {:color "#fff"
    :padding-left (px 5)
    :padding-right (px 5)
    :margin (px 0)}]

  [:.snodeInner
   {:padding (px 5)}]])