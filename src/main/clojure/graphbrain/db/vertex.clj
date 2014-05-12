(ns graphbrain.db.vertex
  (:require [graphbrain.db.id :as id]
            [graphbrain.db.entity :as entity]
            [graphbrain.db.edge :as edge]
            [graphbrain.db.edgetype :as edgetype]
            [graphbrain.db.urlnode :as url]
            [graphbrain.db.user :as user]
            [graphbrain.db.context :as context]
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
    :context (context/id->context id)
    :prog (prog/id->prog id)
    :text (text/id->text id)))

(defn global-space?
  [vertex]
  (id/global-space? (:id vertex)))

(defn local-space?
  [vertex]
  (id/local-space? (:id vertex)))

(defn local->global
  [vertex]
  (let [gid (id/local->global (:id vertex))]
    (if (id/edge? gid)
      (edge/id->edge gid)
      (assoc vertex :id gid))))

(defn global->local
  [vertex owner]
  (let [lid (id/global->local (:id vertex) owner)]
    (if (id/edge? lid)
      (edge/id->edge lid)
      (assoc vertex :id lid))))

(defn label
  [vertex]
  (case (:type vertex)
    :entity (entity/label vertex)
    :user (:name vertex)
    (:id vertex)))
