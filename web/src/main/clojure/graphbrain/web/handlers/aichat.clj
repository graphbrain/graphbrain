(ns graphbrain.web.handlers.aichat
  (:use (graphbrain.web common))
  (:require [clojure.data.json :as json])
  (:import (com.graphbrain.eco Prog)
           (com.graphbrain.db VertexType)))

(def prog
  (Prog/fromString
    (slurp "eco/chat.eco")
    graph))

(defn- aichat-reply
  [root-id vertex]
  (let
    [goto-id (if
               (and vertex (= (. vertex type) VertexType/Edge)
                 (second (. vertex getIds)))
               root-id)]
    (json/write-str {:sentence (,.id vertex)
      :newedges (list (. vertex id))
      :goto goto-id})))

(defn handle-aichat
  [request]
  (let [sentence ((request :form-params) "sentence")
        root-id ((request :form-params) "rootId")
        root (if root-id (. graph get root-id))
        user (get-user request)]
    (. prog setVertex "$user" user)
    (. prog setVertex "$root" root)
    (let
      [ctxts-list (. prog wv sentence 0)
       vertex (. (first (. (first ctxts-list) getCtxts)) getTopRetVertex)]
      (prn (. (last (. (last ctxts-list) getCtxts)) getTopRetVertex))
      ;(. graph put vertex (. user id))
      (prn (aichat-reply root-id vertex)))))