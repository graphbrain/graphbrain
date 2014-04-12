(ns graphbrain.web.handlers.aichat
  (:use (graphbrain.web common)
        (graphbrain.string))
  (:require [clojure.data.json :as json]
            [graphbrain.graph :as gb])
  (:import (com.graphbrain.eco Prog)
           (com.graphbrain.db VertexType)))

(defonce prog
  (Prog/fromString
    (slurp "eco/chat.eco")
    graph))

(defn- aichat-reply
  [root-id vertex sentence]
  (let
      [goto-id (if (gb/edge? vertex)
               (second (. vertex getIds))
               root-id)]
    (json/write-str
     {:sentence sentence
      :newedges (list (. vertex id))
      :gotoid goto-id})))

(defn- sentence-type
  [sentence]
  (if (and (no-spaces? sentence)
           (or (.startsWith sentence "http://") (.startsWith sentence "https://")))
    :url :fact))

(defn- process-fact
  [user root sentence]
  (. prog setVertex "$user" user)
  (. prog setVertex "$root" root)
  (let
      [ctxts-list (. prog wv sentence 0)
       vertex (. (first (. (first ctxts-list) getCtxts)) getTopRetVertex)]
    (if (gb/edge? vertex)
      (. graph put vertex (. user id)))
    (aichat-reply (.-id root) vertex (.-id vertex))))

(defn process-url
  [user root sentence]
  (aichat-reply (.-id root) root "url!"))

(defn handle-aichat
  [request]
  (let [sentence ((request :form-params) "sentence")
        root-id ((request :form-params) "rootId")
        root (if root-id (. graph get root-id))
        user (get-user request)]
    (case (sentence-type sentence)
      :fact (process-fact user root sentence)
      :url (process-url user root sentence))))
