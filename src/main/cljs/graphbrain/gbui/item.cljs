(ns graphbrain.gbui.item
  (:require [jayq.core :as jq]
            [graphbrain.gbui.remove :as rem]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(defn item-div-id
  [edge]
  (if (empty? edge)
    "i_"
    (let [div-id (clojure.string/replace edge #"\W" "_")]
      (str "i_" div-id))))

(defn- url-item-html
  [node div-id]
  (let [title-class "item-url-title"
        url-class "item-url"
        t-div-id (str "t" div-id)
        html (str "<div class='item-main'>")
        html (str html "<div class='" title-class "'" "id='" t-div-id "'>")
        html (str html "<a href='/x/"
                  (js/encodeURIComponent (:id node))
                  "' id='"
                  div-id
                  "'>")
        html (str html (:text node))
        html (str html "</a></div><br />")
        icon (:icon node)
        html (if (not= icon "")
               (str html
                    "<div><img src='"
                    icon
                    "' width='16px' height='16px' class='item-ico' /></div>")
               html)
        html (str html "<div class='" url-class "'>")
        url (:url node)
        html (str html "<a href='" url "' id='url" div-id "'>")
        html (str html url)
        html (str html "</a></div></div>")
        html (str html
                  "<div class='node-remove'><a id='rem"
                  div-id
                  "' href='#'>x</a></div>")
        html (str html "<div style='clear:both;'></div>")]
    html))

(defn- subtext-item
  [sub]
  (if (= (:id sub) "")
    (:text sub)
    (str "<a href='/x/" (:id sub) "'>" (:text sub) "</a>")))

(defn- subtext
  [sub]
  (clojure.string/join ", " (map subtext-item sub)))

(defn- entity-item-html
  [node div-id]
  (let [main-class "item-main"
        title-class "item-title"
        t-div-id (str "t" div-id)
        html (str "<div class='" main-class  "'>")
        html (str html "<div class='" title-class "'" "id='" t-div-id "'>")
        html (str html "<a href='/x/"
                  (js/encodeURIComponent (:id node))
                  "' id='"
                  div-id
                  "'>")
        html (str html (:text node) "&nbsp;")
        html (str html "</a></div>")
        sub-txt (subtext (:sub node))
        html (if sub-txt
               (str html
                    "<div class='item-sub-text'> "
                    sub-txt
                    "</div>")
               html)
        html (str html "</div>")
        html (str html
                  "<div class='item-remove'><a id='rem"
                  div-id
                  "' href='#'>x</a></div>")
        html (str html "<div style='clear:both;'></div>")]
    html))

(defn- item-html
  [node div-id]
  (let [type (:type node)]
    (if (= type "url")
      (url-item-html node div-id)
      (entity-item-html node div-id))))

(defn item-place
  [node snode-id snode]
  (let [class "item"
        div-id (item-div-id (:edge node))
        html (str "<div id='" div-id "' class='" class "'>")
        html (str html (item-html node div-id))
        html (str html "</div>")]
    (jq/append ($ (str "#" snode-id " .viewport")) html)
    (jq/bind ($ (str "#rem" div-id))
                   :click
                   #(rem/remove-clicked node snode))
    node))
