(ns graphbrain.db.gbdb
  (:require [graphbrain.db.mysql :as mysql]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.user :as user]
            [graphbrain.db.queues :as queues]
            [clojure.set])
  (:import (java.util Date)))

(defn gbdb
  ([] (gbdb "gbnode"))
  ([name] mysql/db-connection))

(defn getv
  [gbdb id]
  (mysql/getv gbdb id (id/id->type id)))

(defn exists?
  [gbdb vert-or-id]
  (let [id (if (string? vert-or-id) vert-or-id (:id vert-or-id))
        vtype (id/id->type id)]
    (mysql/exists? gbdb id vtype)))

(defn add-link-to-global!
  [gbdb global-id local-id]
  (mysql/add-link-to-global! gbdb global-id local-id))

(defn remove-link-to-global!
  [gbdb global-id local-id]
  (mysql/remove-link-to-global! gbdb global-id local-id))

(defn- f-degree!
  [gbdb id-or-vertex f]
  (let [vertex (if (string? id-or-vertex) (getv gbdb id-or-vertex) id-or-vertex)
        degree (:degree vertex)
        vertex (assoc vertex :degree (f degree))]
    (mysql/update! gbdb vertex)))

(defn inc-degree!
  [gbdb id-or-vertex]
  (f-degree! gbdb id-or-vertex inc))

(defn dec-degree!
  [gbdb id-or-vertex]
  (f-degree! gbdb id-or-vertex dec))

(defn putv!
  ([gbdb vertex]
     (if (not (exists? gbdb vertex))
       (let [vertex (assoc vertex :ts (.getTime (Date.)))]
         (mysql/putv! gbdb vertex)
         (if (= (:type vertex) :edge)
           (doseq [id (maps/ids vertex)]
             (putv! gbdb (maps/id->vertex id))
             (inc-degree! gbdb id)))))
     vertex)
  ([gbdb vertex owner-id]
     (putv! gbdb vertex)
     (if (not (id/local-space? (:id vertex)))
       (let [lvert (maps/global->local vertex owner-id)]
         (if (not (exists? gbdb lvert))
           (putv! gbdb lvert)
           (add-link-to-global! gbdb (:id vertex) (:id lvert)))
         (if (= (:type vertex) :edge)
            ;; run consensus algorithm
            (let [gedge (maps/local->global vertex)]
              (queues/consensus-enqueue! (:id gedge))))))
     vertex))

(defn update!
  [gbdb vertex]
  (mysql/update! gbdb vertex))

(defn put-or-update!
  [gbdb vertex]
  (if (exists? gbdb vertex)
    (update! gbdb vertex)
    (putv! gbdb vertex)))

(defn edge?
  [vert]
  (id/edge? (:id vert)))

(defn all-users
  [gbdb]
  (mysql/all-users gbdb))

(defn- on-remove-edge!
  [gbdb edge]
  (doseq [id (maps/ids edge)] (dec-degree! gbdb id)))

(defn remove!
  ([gbdb vertex]
     (mysql/remove! gbdb vertex)
     (if (edge? vertex)
       (on-remove-edge! vertex)))
  ([gbdb vertex owner-id]
     (let [u (maps/global->local vertex owner-id)]
       (mysql/remove! gbdb u)
       (remove-link-to-global! gbdb (:id vertex) (:id u))
       (if (edge? u)
         (do (putv! gbdb (maps/negate u))
             (queues/consensus-enqueue! (:id vertex)))))))

(defn pattern->edges
  [gbdb pattern]
  (mysql/pattern->edges gbdb pattern))

(defn id->edges
  ([gbdb center]
     (mysql/id->edges gbdb center))
  ([gbdb center-id owner-id]
     (let [edges (id->edges gbdb center-id)
           gedges (filter maps/global-space? edges)
           ledges (if owner-id
                    (let [lcenter-id (id/global->local center-id owner-id)]
                      (map maps/local->global
                           (filter maps/local-space?
                                   (id->edges gbdb lcenter-id)))) #{})]
       (clojure.set/union (set (filter
                                maps/positive?
                                (filter #(not (some #{(maps/negate %)} ledges))
                                        gedges)))
                          (set (filter maps/positive? ledges))))))

(defn vertex->edges
  [gbdb center]
    (id->edges gbdb (:id center)))

(defn edges->vertex-ids
  [edges]
  (set (flatten (map maps/ids edges))))

(defn neighbors
  [grpah center-id]
  (conj (edges->vertex-ids (id->edges gbdb center-id)) center-id))

(defn email->id
  [gbdb email]
  (let [username (mysql/email->username gbdb email)]
    (if username (id/username->id username))))

(defn username-exists?
  [gbdb username]
  (exists? gbdb (id/username->id username)))

(defn email-exists?
  [gbdb email]
  (mysql/email->username gbdb email))

(defn find-user
  [gbdb login]
  (if (exists? gbdb (id/username->id login))
    (getv gbdb (id/username->id login))
    (let [uid (email->id gbdb login)]
      (if uid
        (if (exists? gbdb uid) (getv gbdb uid))))))

(defn username->vertex
  [gbdb username]
  (let [uid (id/username->id username)]
    (if (exists? gbdb uid) (getv gbdb uid))))

(defn create-user!
  [gbdb username name email password role]
  (putv! gbdb (user/new-user username name email password role)))

(defn attempt-login!
  [gbdb login password]
  (let [user (find-user gbdb login)]
    (if (and user (user/check-password user password))
        (update! gbdb (user/new-session user)))))

(defn force-login!
  [login]
  (let [user (find-user login)]
    (if user
        (update! (user/new-session user)))))

(defn all-users [gbdb] (mysql/all-users gbdb))

(defn global-alts
  [gbdb global-id]
  (mysql/alts gbdb global-id))

(defn description
  [gbdb id-or-vertex]
  (let [id (if (string? id-or-vertex) id-or-vertex (:id id-or-vertex))
        vertex (if (string? id-or-vertex) (getv gbdb id-or-vertex) id-or-vertex)
        as-in (pattern->edges gbdb ["r/+type_of" id "*"])
        desc (vertex/label vertex)]
    (if (empty? as-in) desc
        (str desc " ("
             (clojure.string/join
              ", "
              (map #(vertex/label (getv gbdb (second (maps/participant-ids %))))
                   as-in)) ")"))))
