(ns graphbrain.web.css.eco
  (:require [garden.units :refer [px]]))

(def css
  [[:#eco-results
    {:max-width (px 900)
     :height "100%"
     :padding-top (px 100)
     :margin-left "auto"
     :margin-right "auto"
     :display "block"
     :font-size "14pt"}]

   [:.eco-section
    {:margin (px 20)
     :background "#FFF"}]

   [:.eco-word
    {:margin-right (px 10)
     :display "inline"}]

   [:.eco-pos
    {:color "#00CC00"}]

   [:.eco-lemma
    {:color "#0080FF"}]

   [:.eco-trace-box
    {:margin "5px 5px 5px 30px"
     :padding (px 10)
     :border-style "solid"
     :border-width (px 1)
     :border-color "#AAA"}]

   [:.eco-weight
    {:color "#800000"}]

   [:.eco-rule
    {:color "#0000FF"}]

   [:.eco-vertex
    {:color "#808080"}]])
