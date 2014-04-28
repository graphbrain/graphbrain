(ns graphbrain.db.vertex
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.entity :as entity]
            [graphbrain.db.edge :as edge]
            [graphbrain.db.edgetype :as edgetype]
            [graphbrain.db.urlnode :as url]
            [graphbrain.db.user :as user]
            [graphbrain.db.prog :as prog]
            [graphbrain.db.text :as text]))

(defn id->vertex
  [id]
  (case (id/id->type id)
    :entity (entity/id->entity id)
    :edge (edge/id->edge id)
    :edge-type (edgetype/id->edgetype id)
    :url (url/id->urlnode id)
    :user (user/id->user id)
    :prog (prog/id->prog id)
    :text (text/id->text id)))

(defn user->global
  [vertex]
  (case (:type vertex)
    :edge (edge/id->edge (id/user->global (:id vertex)))
    :user vertex
    (assoc vertex :id (id/user->global (:id vertex)))))

(defn global->user
  [vertex user-id]
  (case (:type vertex)
    :edge (edge/id->edge (id/global->user (:id vertex) user-id))
    :user vertex
    (assoc vertex :id (id/global->user (:id vertex) user-id))))

(defn global-space?
  [vertex]
  (id/global-space? (:id vertex)))

(defn user-space?
  [vertex]
  (id/user-space? (:id vertex)))

(defn label
  [vertex]
  (case (:type vertex)
    :entity (entity/label vertex)
    :user (:name vertex)
    (:id vertex)))
