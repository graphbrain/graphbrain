(ns graphbrain.web.css.general
  (:require [garden.units :refer [px]]))

(def css
  [[:html :body
   {:width "100%"
    :height "100%"
    :margin (px 0)
    :font-family "Helvetica, sans-serif"
    :font-size (px 12)
    :background "#fff"}]

  [:.landing
   {:padding (px 50)
    :margin (px 20)
    :border-radius (px 10)}]

  [:#main-view
   {:width "100%"
    :height "100%"
    :background "rgba(10, 10, 60, 0.8)"
    :z-index 1}]

  [:#data-view
   {:position "relative"
    :width "100%"
    :height "100%"
    :z-index 1}]

  [:#graph-view
   {:float "left"
    :width "75%"
    :height "100%"
    :-webkit-perspective (px 1000)
    :-moz-perspective (px 1000)
    :-webkit-transform-style "preserve-3d"
    :-moz-transform-style "preserve-3d"}]])
