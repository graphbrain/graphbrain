(ns graphbrain.db.urlnode
  (:require [graphbrain.db.id :as id]))

(defn id->url
  [id]
  (clojure.string/join "/" (rest (id/parts id))))

(defn url->id
  [url]
  (str "h/" url))

(defn id->urlnode
  ([id degree ts]
     {:id id
      :type :url
      :title ""
      :icon ""
      :degree degree
      :ts ts})
  ([id]
     (id->urlnode id 0 -1)))

(defn url
  [node]
  (id->url (:id node)))
