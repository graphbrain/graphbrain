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
  ([gbdb id]
     (mysql/getv gbdb id (id/id->type id)))
  ([gbdb id ctxts]
     (loop [cs ctxts
            vertex nil]
       (if (or vertex (empty? cs))
         vertex
         (recur (rest cs) (getv gbdb (id/global->local id (first cs))))))))

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
  [gbdb vertex owner-id]
  (let [gvert (maps/local->global vertex)
        lvert (maps/global->local gvert owner-id)]
    (if (not (exists? gbdb lvert))
      (let [lvert (assoc lvert :ts (.getTime (Date.)))]
        (mysql/putv! gbdb lvert)
        (add-link-to-global! gbdb (:id gvert) (:id lvert))
        (case (:type lvert)
          :entity (if (> (id/count-parts (:id gvert)) 1)
                    (let [vid (:eid gvert)
                          sid (id/last-part (:id gvert))
                          rel (maps/id->vertex
                               (str "(r/+can_mean " sid " " vid ")"))]
                      (putv! gbdb rel owner-id)))
          :edge (doseq [id (maps/ids lvert)]
                  (let [v (maps/id->vertex id)]
                    (putv! gbdb v owner-id)
                    (inc-degree! gbdb (:id v))))
          nil)
        vertex)
      vertex)))

(defn update!
  [gbdb vertex]
  (mysql/update! gbdb vertex))

(defn all-users
  [gbdb]
  (mysql/all-users gbdb))

(defn remove!
  [gbdb vertex owner-id]
  (let [gvert (maps/local->global vertex)
        lvert (maps/global->local gvert owner-id)]
    (mysql/remove! gbdb lvert)
    (remove-link-to-global! gbdb (:id gvert) (:id lvert))
    (if (maps/edge? lvert)
      (do (putv! gbdb (maps/negate gvert) owner-id)
          (doseq [id (maps/ids lvert)]
            (dec-degree! gbdb (id/eid->id id)))))))

(defn id->eid
  [gbdb id]
  (if (= (id/id->type id) :entity)
    (let [eid (:eid (getv gbdb id))]
      (if eid eid id)) id))

(defn- patid->local-eid
  [gbdb patid owner-id]
  (if (= patid "*")
    patid
    (id/global->local (id->eid gbdb patid) owner-id)))

(defn f->edges
  [f ctxts]
  (let [edges-sets (map f ctxts)
        edges-sets (map #(map maps/local->global %) edges-sets)
        edges (into #{} (apply clojure.set/union edges-sets))
        edges (filter #(not (some #{(maps/negate %)} edges)) edges)
        edges (filter maps/positive? edges)]
    edges))

(defn pattern->edges
  [gbdb pattern ctxts]
  (let [f (fn [ctxt]
            (mysql/pattern->edges
             gbdb
             (map #(patid->local-eid gbdb % ctxt) pattern)))]
    (f->edges f ctxts)))

(defn id->edges
  [gbdb id ctxts]
  (let [f #(mysql/id->edges gbdb (id->eid gbdb (id/global->local id %)))]
    (f->edges f ctxts)))

(defn vertex->edges
  [gbdb center ctxts]
    (id->edges gbdb (:id center) ctxts))

(defn edges->vertex-ids
  [edges]
  (set (flatten (map maps/ids edges))))

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
  (putv! gbdb (user/new-user username name email password role) ""))

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

(defn remove-context!
  [gbdb ctxt]
  (mysql/remove-context! gbdb ctxt))
