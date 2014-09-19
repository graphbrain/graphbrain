(ns graphbrain.web.css.frame
  (:require [garden.units :refer [px]]))

(def css
  [[:.frame
   {:font-size (px 14)
    :border-style "none"
    :width "100%"
    :margin-bottom (px 15)
    :display "inline-block"
    :background "#FFF"
    :box-shadow "1px 1px 1px #CCC"}]

  [:.frame-label
   {:padding-left (px 5)
    :padding-right (px 5)
    :margin (px 0)
    :font-weight "bold"}]

  [:.frame-inner
   {}]])
