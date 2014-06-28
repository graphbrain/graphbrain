(ns graphbrain.web.handlers.aichat
  (:require [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [clojure.data.json :as json]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.graphjava :as gbj]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.urlnode :as url]
            [graphbrain.disambig.entityguesser :as eg]
            [graphbrain.braingenerators.pagereader :as pr]
            [graphbrain.string :as gbstr])
  (:import (com.graphbrain.eco Prog)))

(def prog
  (Prog/fromString
    (slurp "eco/chat.eco")
    #_(gbj/graph)
    nil))

(defn- aichat-reply
  [root-id vertex sentence]
  (let [goto-id (if (maps/edge? vertex)
                  (second (maps/ids vertex))
                  root-id)]
    (json/write-str {:sentence sentence
                     :newedges (list (:id vertex))
                     :gotoid goto-id})))

(defn- sentence-type
  [sentence]
  (if (and (gbstr/no-spaces? sentence)
           (or (.startsWith sentence "http://") (.startsWith sentence "https://")))
    :url :fact))

(defn- disambig-id
  [id eid ctxts]
  (if (= (id/id->type id) :entity)
    (eg/guess-eid common/gbdb (id/last-part id) eid ctxts)
    id))

(defn- disambig-edge
  [edge eid ctxts]
  (let [ids (maps/ids edge)
        ids (map #(disambig-id % eid ctxts) ids)]
    (maps/ids->edge ids)))

(defn- process-fact
  [user root sentence ctxts]
  (. prog setVertex "$user" (gbj/map->user-obj user))
  (. prog setVertex "$root" (gbj/map->vertex-obj root))
  (let
      [ctxts-list (. prog wv sentence 0)
       vertex (gbj/vertex-obj->map
               (. (first (. (first ctxts-list) getCtxts)) getTopRetVertex))]
    (if (maps/edge? vertex)
      (let [edge (disambig-edge vertex "xpto" ctxts)
            edge (assoc edge :score 1)]
        (gb/putv! common/gbdb edge (:id user))
        (aichat-reply (:id root) edge (:id edge))))))

(defn process-url
  [user root sentence ctxts]
  (let [url-id (url/url->id sentence)]
    (pr/extract-knowledge! common/gbdb sentence ctxts (:id user))
    (aichat-reply url-id nil (str "processed url: " sentence))))

(defn handle-aichat
  [request]
  (let [sentence ((request :form-params) "sentence")
        root-id ((request :form-params) "rootId")
        user (common/get-user request)
        root (if root-id (gb/getv common/gbdb root-id
                                  (contexts/active-ctxts request user)))
        ctxts (contexts/active-ctxts request user)]
    (case (sentence-type sentence)
      :fact (process-fact user root sentence ctxts)
      :url (process-url user root sentence ctxts))))
