(ns graphbrain.web.handlers.aichat
  (:require [graphbrain.web.common :as common]
            [clojure.data.json :as json]
            [graphbrain.db.graph :as gb]
            [graphbrain.db.edge :as edge]
            [graphbrain.db.urlnode :as url]
            [graphbrain.braingenerators.pagereader :as pr]
            [graphbrain.string :as gbstr])
  (:import (com.graphbrain.eco Prog)))

(defonce prog
  (Prog/fromString
    (slurp "eco/chat.eco")
    common/graph))

(defn- aichat-reply
  [root-id vertex sentence]
  (let [goto-id (if (gb/edge? vertex)
                  (second (:ids vertex))
                  root-id)]
    (json/write-str {:sentence sentence
                     :newedges (list (:id vertex))
                     :gotoid goto-id})))

(defn- sentence-type
  [sentence]
  (if (and (gbstr/no-spaces? sentence)
           (or (.startsWith sentence "http://") (.startsWith sentence "https://")))
    :url :fact))

(defn- process-fact
  [user root sentence]
  (. prog setVertex "$user" (gb/map->user-obj user))
  (. prog setVertex "$root" (gb/map->vertex-obj root))
  (let
      [ctxts-list (. prog wv sentence 0)
       vertex (gb/vertex-obj->map
               (. (first (. (first ctxts-list) getCtxts)) getTopRetVertex))]
    (if (gb/edge? vertex)
      (gb/putv! common/graph vertex (:id user)))
    (aichat-reply (:id root) vertex (:id vertex))))

(defn process-url
  [user root sentence]
  (let [url-id (url/url->id sentence)]
    (pr/extract-knowledge! sentence)
    (aichat-reply (:id root) root "url!")))

(defn handle-aichat
  [request]
  (let [sentence ((request :form-params) "sentence")
        root-id ((request :form-params) "rootId")
        root (if root-id (gb/getv common/graph root-id))
        user (common/get-user request)]
    (case (sentence-type sentence)
      :fact (process-fact user root sentence)
      :url (process-url user root sentence))))
