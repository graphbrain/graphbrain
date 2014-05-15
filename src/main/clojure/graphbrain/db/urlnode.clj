(ns graphbrain.db.urlnode
  (:require [graphbrain.db.id :as id]))

(defn id->url
  [id]
  (clojure.string/join "/" (rest (id/parts id))))

(defn url->id
  [url]
  (str "h/" url))

(defn id->urlnode
  [id]
  {:id id
   :type :url})

(defn url
  [node]
  (id->url (:id node)))
