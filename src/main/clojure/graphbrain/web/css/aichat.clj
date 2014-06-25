(ns graphbrain.web.css.aichat
  (:require [garden.units :refer [px]]))

(def css
  [[:#ai-chat
   {:position "absolute"
    :bottom 0
    :right 0
    :float "right"
    :width "25%"
    :height "100%"
    :color "#fff"
    :background "rgba(0, 0, 0, 1)"
    :padding-left (px 10)
    :padding-right (px 10)
    :overflow "auto"}]

  [:#ai-chat-input
   {:margin-top (px 10)
    :margin-bottom (px 5)
    :padding (px 5)
    :border-width (px 0)
    :width "100%"
    :line-height "100%"
    :color "#000"
    :font-size (px 12)}]

  [:.aichat_action
   {:background "#69D2E7"}]

  [:.gb-line
   {:color "#888"}]

  [:.gb-line
   [:a
    {:color "#888"}]]

  [:.gb-line
   [:a:hover
    {:color "#fff"
     :text-decoration "underline"}]]

  [:.user-line
   {:color "#fff"}]])
