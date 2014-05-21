(ns graphbrain.db.maps
  (:require [graphbrain.db.id :as id]))


;; edges

(defn id->edge
  ([id score]
     {:id id
      :type :edge
      :score score})
  ([id]
     (id->edge id 1)))

(defn ids
  [edge]
  (id/id->ids (:id edge)))

(defn participant-ids
  [edge]
  (rest (ids edge)))

(defn edge-type
  [edge]
  (first (ids edge)))

(defn idedge->entity
  [typerel]
  (if (= (edge-type typerel) "r/+id")
    (let [hsh (id/hashed (:id typerel))
          name (first (participant-ids typerel))
          id (id/build [hsh name])]
      {:id id
       :type :entity
       :typerel (:id typerel)})))

(defn idedge-id->entity
  [idedge-id]
  (if (= (id/id->type idedge-id) :edge)
    (idedge->entity (id->edge idedge-id))))

(defn ids->edge
  ([ids score]
     (id->edge (id/ids->id ids) score))
  ([ids]
     (ids->edge ids 1)))


;; entity

(defn id->entity
  [id]
  {:id id
   :type :entity})


;; context

(defn id->context
  [id]
  {:id id
   :type :context})


;; edgetype

(defn id->edgetype
  [id]
  {:id id
   :type :edge-type})


;; prog

(defn id->prog
  [id]
  {:id id
   :type :prog})


;; text

(defn id->text
  [id]
  {:id id
   :type :text})


;; url

(defn id->urlnode
  [id]
  {:id id
   :type :url})


;; user

(defn id->user
  [id]
  {:id id
   :type :user
   :username (id/last-part id)})


;; negations

(defn negative-id?
  [id]
  (= (first (id/parts (id/local->global id))) "n"))

(defn positive-id?
  [id]
  (not (negative-id? id)))

(defn negate-id
  [id]
  (if (negative-id? id) id
    (let [owner-id (id/owner id)
          global-id (id/local->global id)
          neg-id (str "n/" global-id)]
      (if (empty? owner-id)
        neg-id
        (id/global->local neg-id owner-id)))))

(defn negate-edge
  [edge]
  (ids->edge (cons (negate-id (edge-type edge)) (participant-ids edge))))

(defn negative-edge?
  [edge]
  (negative-id? (edge-type edge)))

(defn positive-edge?
  [edge]
  (not (negative-edge? edge)))

(defn negate
  [thing]
  (cond
   (string? thing) (negate-id thing)
   (= (id/id->type (:id thing)) :edge) (negate-edge thing)
   :else nil))

(defn negative?
  [thing]
  (cond
   (string? thing) (negative-id? thing)
   (= (id/id->type (:id thing)) :edge) (negative-edge? thing)
   :else nil))

(defn positive?
  [thing]
  (cond
   (string? thing) (positive-id? thing)
   (= (id/id->type (:id thing)) :edge) (positive-edge? thing)
   :else nil))


;; generic

(defn id->vertex
  [id]
  (case (id/id->type id)
    :entity (id->entity id)
    :edge (id->edge id)
    :edge-type (id->edgetype id)
    :url (id->urlnode id)
    :user (id->user id)
    :context (id->context id)
    :prog (id->prog id)
    :text (id->text id)))

(defn local->global
  [vertex]
  (let [gid (id/local->global (:id vertex))]
    (if (id/edge? gid)
      (id->edge gid (:score vertex))
      (assoc vertex :id gid))))

(defn global->local
  [vertex owner]
  (let [lid (id/global->local (:id vertex) owner)]
    (if (id/edge? lid)
      (id->edge lid (:score vertex))
      (assoc vertex :id lid))))

(defn global-space?
  [vertex]
  (id/global-space? (:id vertex)))

(defn local-space?
  [vertex]
  (id/local-space? (:id vertex)))
