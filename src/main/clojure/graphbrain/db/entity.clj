(ns graphbrain.db.entity
  (:require [graphbrain.db.id :as id]))

(defn id->entity
  [id]
  {:id id
   :type :entity
   :degree -1
   :ts -1})

(defn text
  [entity]
  (clojure.string/replace (id/last-part (:id entity)) "_" " "))

(defn label
  [entity]
  (let [desc (text entity)]
    (clojure.string/join (cons (.toUpperCase (str (first desc))) (rest desc)))))
