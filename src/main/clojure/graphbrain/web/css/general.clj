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

  [:#rel-list
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
    {:color "rgba(0, 0, 0, 1)"}]]

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
    :-moz-transform-style "preserve-3d"}]

  [:#ai-chat
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
    :padding-left (px 0)
    :padding-right (px 0)
    :border-width (px 0)
    :width "100%"}]

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
   {:color "#fff"}]

  [:.navbar-container
   {:position "absolute"
    :top (px 0)
    :left (px 0)
    :heigth (px 45)
    :background "rgba(0, 0, 0, 0.5)"
    :z-index 10
    :padding-left (px 15)
    :padding-right (px 15)}]

  [:.navbar-element
   {:display "inline"
    :line-height (px 45)}]

  [:.menu-caret
   {:display "inline-block"
    :width (px 0)
    :height (px 0)
    :vertical-align "middle"
    :border-top "5px solid #aaa"
    :border-right "5px solid transparent"
    :border-left "5px solid transparent"
    :color "#aaa"}]

  [:.nav-search
   {:display "inline"
    :margin-left (px 15)
    :margin-right (px 15)}]

  [:.nav-search
   ["input[type=text]"
    {:padding (px 5)
     :margin (px 0)
     :border "1px solid #000" 
     :-webkit-border-radius (px 0)
     :border-radius (px 0)}]]])
