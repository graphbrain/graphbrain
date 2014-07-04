(ns graphbrain.gbui.node
  (:require [jayq.core :as jq]
            [graphbrain.gbui.remove :as rem]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(defn node-div-id
  [edge]
  (if (empty? edge)
    "n_"
    (let [div-id (clojure.string/replace edge #"\W" "_")]
      (str "n_" div-id))))

(defn- url-node-html
  [node root div-id]
  (let [title-class (if root "node-title-root" "node-url-title")
        url-class (if root "node-url-root" "node-url")
        t-div-id (str "t" div-id)
        html (str "<div class='node-main'>")
        html (str html "<div class='" title-class "'" "id='" t-div-id "'>")
        html (str html "<a href='/node/"
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
                    "' width='16px' height='16px' class='node-ico' /></div>")
               html)
        html (str html "<div class='" url-class "'>")
        url (:url node)
        html (str html "<a href='" url "' id='url" div-id "'>")
        html (str html url)
        html (str html "</a></div></div>")
        html (if root
               html
               (str html
                    "<div class='node-remove'><a id='rem"
                    div-id
                    "' href='#'>x</a></div>"))
        html (str html "<div style='clear:both;'></div>")]
    html))

(defn- subtext-item
  [sub]
  (if (= (:id sub) "")
    (:text sub)
    (str "<a href='/node/" (:id sub) "'>" (:text sub) "</a>")))

(defn- subtext
  [sub]
  (clojure.string/join ", " (map subtext-item sub)))

(defn- entity-node-html
  [node root div-id]
  (let [main-class (if root "node-main-root" "node-main")
        title-class (if root "node-title-root" "node-title")
        t-div-id (str "t" div-id)
        html (str "<div class='" main-class  "'>")
        html (str html "<div class='" title-class "'" "id='" t-div-id "'>")
        html (str html "<a href='/node/"
                  (js/encodeURIComponent (:id node))
                  "' id='"
                  div-id
                  "'>")
        html (str html (:text node) "&nbsp;")
        html (str html "</a></div>")
        sub-txt (subtext (:sub node))
        html (if sub-txt
               (str html
                    "<div class='node-sub-text'> "
                    sub-txt
                    "</div>")
               html)
        html (str html "</div>")
        html (if root
               html
               (str html
                    "<div class='node-remove'><a id='rem"
                    div-id
                    "' href='#'>x</a></div>"))
        html (str html "<div style='clear:both;'></div>")]
    html))

(defn- node-html
  [node root div-id]
  (let [type (:type node)]
    (if (= type "url")
      (url-node-html node root div-id)
      (entity-node-html node root div-id))))

(defn node-place
  [node snode-id snode root]
  (let [class (if root "node-root" "node")
        div-id (node-div-id (:edge node))
        html (str "<div id='" div-id "' class='" class "'>")
        html (str html (node-html node root div-id))
        html (str html "</div>")]
    (jq/append ($ (str "#" snode-id " .viewport")) html)
    (jq/bind ($ (str "#rem" div-id))
                   :click
                   #(rem/remove-clicked node snode))
    node))
