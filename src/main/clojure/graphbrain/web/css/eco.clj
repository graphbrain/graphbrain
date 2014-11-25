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
    {:color "#0080FF"}]])
