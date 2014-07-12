(ns graphbrain.db.urlnode
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.text :as text]))

(defn id->url
  [id]
  (clojure.string/join "/" (rest (id/parts id))))

(defn url->id
  [url]
  (str "h/" url))

(defn url
  [node]
  (id->url (:id node)))

(defn title
  [gbdb id ctxts]
  (let [title-edges (gb/pattern->edges gbdb ["r/*title" id "*"] ctxts)]
    (if (empty? title-edges) ""
        (let [title-edge (first title-edges)
              title-id (second (maps/participant-ids title-edge))
              title-node (text/id->text gbdb title-id)]
          (:text title-node)))))
