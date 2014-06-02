(ns graphbrain.db.entity
  (:require [graphbrain.db.id :as id]))

(defn text
  [id-or-entity]
  (let [id (if (string? id-or-entity)
             (id/eid->id id-or-entity)
             (:id id-or-entity))]
    (clojure.string/replace (id/last-part id) "_" " ")))

(defn label
  [id-or-entity]
  (let [desc (text id-or-entity)]
    (clojure.string/join (cons (.toUpperCase (str (first desc))) (rest desc)))))

(defn description
  [eid-or-entity]
  (if (string? eid-or-entity)
    (let [ids (id/id->ids eid-or-entity)
          ents (rest ids)
          ents (map label ents)
          name (first ents)
          classes (rest ents)]
      (str name " (" (clojure.string/join ", " classes) ")"))
    (let [eid (:eid eid-or-entity)]
      (if eid
        (description eid)
        (label eid-or-entity)))))
