(ns graphbrain.web.handlers.aichat
  (:require [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [clojure.data.json :as json]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.urlnode :as url]
            [graphbrain.disambig.edgeguesser :as edg]
            [graphbrain.braingenerators.pagereader :as pr]
            [graphbrain.eco.eco :as eco]
            [graphbrain.eco.parsers.chat :as chat]
            [graphbrain.string :as gbstr]))

(defn- aichat-reply
  [root-id vertex sentence]
  (let [goto-id (if (maps/edge? vertex)
                  (id/eid->id (second (maps/ids vertex)))
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
  [user root sentence ctxts]
  (let
      [env {:root (maps/vertex->eid root) :user (:id user)}
       res (eco/parse-str chat/chat sentence env)]
    (if (id/edge? res)
      (let [edge-id (edg/guess common/gbdb res sentence ctxts)
            edge (maps/id->vertex edge-id)
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
