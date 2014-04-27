(ns graphbrain.db.entity
  (:require [graphbrain.db.id :as id]))

(defn id->entity
  ([id degree ts]
     {:id id
      :type :entity
      :degree degree
      :ts ts})
  ([id]
     (id->entity id 0 -1)))

(defn label
  [entity]
  (let [desc (:text entity)]
    (clojure.string/join (cons (.toUpperCase (str (first desc))) (rest desc)))))
