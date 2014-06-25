(ns graphbrain.web.css.relations
  (:require [garden.units :refer [px]]))

(def css
  [[:#rel-list
   {:position "fixed"
    :top (px 45)
    :left (px 0)
    :max-width (px 250)
    :max-height "100%"
    :overflow "auto"
    :padding (px 10)
    :color "rgba(0, 0, 0, 1)"
    :background "rgba(255, 255, 255, 0.95)"
    :border-right-style "solid"
    :border-right-width (px 1)
    :border-right-color "#CCC"
    :z-index 2}]

  [:#rel-list
   [:.visible_rel_link
    {:color "rgba(90, 90, 90, 1)"}]]

  [:#rel-list
   [:.hidden_rel_link
    {:color "rgba(0, 0, 0, 1)"}]]])
