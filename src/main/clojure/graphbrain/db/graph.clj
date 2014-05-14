(ns graphbrain.db.graph
  (:require [graphbrain.db.mysql :as mysql]
            [graphbrain.db.id :as id]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.edge :as edge]
            [graphbrain.db.user :as user]
            [graphbrain.db.queues :as queues]
            [clojure.set])
  (:import (java.util Date)))

(defn graph
  ([] (graph "gbnode"))
  ([name] (mysql/db-spec name)))

(defn getv
  [graph id]
  (mysql/getv graph id (id/id->type id)))

(defn exists?
  [graph vert-or-id]
  (let [id (if (string? vert-or-id) vert-or-id (:id vert-or-id))
        vtype (id/id->type id)]
    (mysql/exists? graph id vtype)))

(defn add-link-to-global!
  [graph global-id local-id]
  (mysql/add-link-to-global! graph global-id local-id))

(defn remove-link-to-global!
  [graph global-id local-id]
  (mysql/remove-link-to-global! graph global-id local-id))

(defn- f-degree!
  [graph id-or-vertex f]
  (let [vertex (if (string? id-or-vertex) (getv graph id-or-vertex) id-or-vertex)
        degree (:degree vertex)
        vertex (assoc vertex :degree (f degree))]
    (mysql/update! graph vertex)))

(defn inc-degree!
  [graph id-or-vertex]
  (f-degree! graph id-or-vertex inc))

(defn dec-degree!
  [graph id-or-vertex]
  (f-degree! graph id-or-vertex dec))

(defn putv!
  ([graph vertex]
     (if (not (exists? graph vertex))
       (let [vertex (assoc vertex :ts (.getTime (Date.)))]
         (mysql/putv! graph vertex)
         (if (= (:type vertex) :edge)
           (doseq [id (edge/ids vertex)]
             (putv! graph (vertex/id->vertex id))
             (inc-degree! graph id)))))
     vertex)
  ([graph vertex owner-id]
     (putv! graph vertex)
     (if (not (id/local-space? (:id vertex)))
       (let [lvert (vertex/global->local vertex owner-id)]
         (if (not (exists? graph lvert))
           (putv! graph lvert)
           (add-link-to-global! graph (:id vertex) (:id lvert)))
         (if (= (:type vertex) :edge)
            ;; run consensus algorithm
            (let [gedge (vertex/local->global vertex)]
              (queues/consensus-enqueue! (:id gedge))))))
     vertex))

(defn update!
  [graph vertex]
  (mysql/update! graph vertex))

(defn put-or-update!
  [graph vertex]
  (if (exists? graph vertex)
    (update! graph vertex)
    (putv! graph vertex)))

(defn edge?
  [vert]
  (id/edge? (:id vert)))

(defn all-users
  [graph]
  (mysql/all-users graph))

(defn- on-remove-edge!
  [graph edge]
  (doseq [id (edge/ids edge)] (dec-degree! graph id)))

(defn remove!
  ([graph vertex]
     (mysql/remove! graph vertex)
     (if (edge? vertex)
       (on-remove-edge! vertex)))
  ([graph vertex owner-id]
     (let [u (vertex/global->local vertex owner-id)]
       (mysql/remove! graph u)
       (remove-link-to-global! graph (:id vertex) (:id u))
       (if (edge? u)
         (do (putv! graph (edge/negate u))
             (queues/consensus-enqueue! (:id vertex)))))))

(defn pattern->edges
  [graph pattern]
  (mysql/pattern->edges graph pattern))

(defn id->edges
  ([graph center]
     (mysql/id->edges graph center))
  ([graph center-id owner-id]
     (let [edges (id->edges graph center-id)
           gedges (filter vertex/global-space? edges)
           ledges (if owner-id
                    (let [lcenter-id (id/global->local center-id owner-id)]
                      (map vertex/local->global
                           (filter vertex/local-space?
                                   (id->edges graph lcenter-id)))) #{})]
       (clojure.set/union (set (filter
                                edge/positive?
                                (filter #(not (some #{(edge/negate %)} ledges))
                                        gedges)))
                          (set (filter edge/positive? ledges))))))

(defn vertex->edges
  [graph center]
    (id->edges graph (:id center)))

(defn edges->vertex-ids
  [edges]
  (set (flatten (map edge/ids edges))))

(defn neighbors
  [grpah center-id]
  (conj (edges->vertex-ids (id->edges graph center-id)) center-id))

(defn email->id
  [graph email]
  (let [username (mysql/email->username graph email)]
    (if username (id/username->id username))))

(defn username-exists?
  [graph username]
  (exists? graph (id/username->id username)))

(defn email-exists?
  [graph email]
  (mysql/email->username graph email))

(defn find-user
  [graph login]
  (if (exists? graph (id/username->id login))
    (getv graph (id/username->id login))
    (let [uid (email->id graph login)]
      (if uid
        (if (exists? graph uid) (getv graph uid))))))

(defn username->vertex
  [graph username]
  (let [uid (id/username->id username)]
    (if (exists? graph uid) (getv graph uid))))

(defn create-user!
  [graph username name email password role]
  (putv! graph (user/new-user username name email password role)))

(defn attempt-login!
  [graph login password]
  (let [user (find-user graph login)]
    (if (and user (user/check-password user password))
        (update! graph (user/new-session user)))))

(defn force-login!
  [login]
  (let [user (find-user login)]
    (if user
        (update! (user/new-session user)))))

(defn all-users [graph] (mysql/all-users graph))

(defn global-alts
  [graph global-id]
  (mysql/alts graph global-id))

(defn description
  [graph id-or-vertex]
  (let [id (if (string? id-or-vertex) id-or-vertex (:id id-or-vertex))
        vertex (if (string? id-or-vertex) (getv graph id-or-vertex) id-or-vertex)
        as-in (pattern->edges graph ["r/+type_of" id "*"])
        desc (vertex/label vertex)]
    (if (empty? as-in) desc
        (str desc " ("
             (clojure.string/join
              ", "
              (map #(vertex/label (getv graph (second (edge/participant-ids %))))
                   as-in)) ")"))))
