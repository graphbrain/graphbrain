(ns graphbrain.web.css.nodepage
  (:require [garden.units :refer [px]]))

(def css
  [[:#nodeback
    {:width "100%"
     :height "100%"
     :background "#EEE"}]
   
   [:#nodepage
    {:max-width (px 900)
     :margin-top (px 50)
     :margin-left "auto"
     :margin-right "auto"
     :display "block"}]

   [:#frames
    {:column-count 3
     :-webkit-column-count 3
     :-mozcolumn-count 3}]

   [:.np-title
    {:font-size (px 36)}]

   [:.np-desc
    {:font-size (px 20)
     :margin-bottom (px 10)}]])
