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

(defn ids->edge
  ([ids score]
     (id->edge (id/ids->id ids) score))
  ([ids]
     (ids->edge ids 1)))

(defn edge?
  [vert]
  (if vert (id/edge? (:id vert))))

;; entity

(defn eid->entity
  [eid]
  (if (id/eid? eid)
    {:id (id/eid->id eid)
     :eid eid
     :type :entity}))

(defn name+ids->entity
  [rel name classes]
  (eid->entity (id/name+ids->eid rel name classes)))

(defn id->entity
  [id]
  {:id id
   :eid id
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
    :edge (if (id/eid? id)
            (eid->entity id)
            (id->edge id))
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

(defn vertex->eid
  [vertex]
  (if (:eid vertex)
    (:eid vertex)
    (:id vertex)))
