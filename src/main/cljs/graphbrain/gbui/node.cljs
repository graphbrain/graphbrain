(ns graphbrain.gbui.node
  (:require [jayq.core :as jq]
            [graphbrain.gbui.remove :as rem])
  (:use [jayq.core :only [$]]))

(defn node-div-id
  [node]
  (let [div-id (str (:id node) (:edge node))
        div-id (clojure.string/replace div-id " " "_")
        div-id (clojure.string/replace div-id "(" "_")
        div-id (clojure.string/replace div-id ")" "_")
        div-id (clojure.string/replace div-id "/" "_")]
    (str "n_" div-id)))

(defn- url-node-html
  [node root div-id]
  (let [title-class (if root "nodeTitle_root" "nodeTitle")
        url-class (if root "nodeUrl_root" "nodeUrl")
        t-div-id (str "t" div-id)
        html (str "<div class='" title-class "'" "id='" t-div-id "'>")
        html (str html "<a href='/node/" (:id node) "' id='" div-id "'>")
        html (str html (:text node))
        html (str "</a></div><br />")
        icon (:icon node)
        html (if (not= icon "")
               (str html
                    "<img src='"
                    icon
                    "' width='16px' height='16px' class='nodeIco' />")
               html)
        html (str html "<div class='" url-class "'>")
        url (:url node)
        html (str html "<a href='" url "' id='url" div-id "'>")
        html (str html url)
        html (str "</a></div>")
        html (if root
               html
               (str html
                    "<div class='nodeRemove'><a id='rem"
                    div-id
                    "' href='#'>x</a></div>"))
        html (str html "<div style='clear:both;'></div>")]
    html))

(defn- entity-node-html
  [node root div-id]
  (let [title-class (if root "nodeTitle_root" "nodeTitle")
        t-div-id (str "t" div-id)
        html (str "<div class='" title-class "'" "id='" t-div-id "'>")
        html (str html "<a href='/node/" (:id node) "' id='" div-id "'>")
        html (str html (:text node))
        html (str html "</a></div>")
        text2 (:text2 node)
        html (if text2
               (str html
                    "<div class='nodeSubText'>("
                    text2
                    ")</div>")
               html)
        html (if root
               html
               (str html
                    "<div class='nodeRemove'><a id='rem"
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
  [node snode-id snode glow]
  (let [root (nil? snode)
        class (if root "node_root" "node")
        div-id (node-div-id node)
        html (str "<div id='" div-id "' class='" class "'>")
        html (str html (node-html node root div-id))
        html (str html "</div>")]
    (jq/append ($ (str "#" snode-id " .viewport")) html)
    (jq/bind ($ (str "#rem" div-id))
                   :click
                   #(rem/remove-clicked node snode))
    ;; (if glow (anim/add-anim (anim/anim-node-glow node)))
    node))
