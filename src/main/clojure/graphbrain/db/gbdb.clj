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
         (if vertex vertex
           (let [id (mysql/first-alt gbdb (id/local->global id))
                 vertex (if id (maps/local->global (getv gbdb id)))]
             vertex))
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

(defn degree
  [gbdb id ctxts]
  (let [v (getv gbdb id ctxts)]
    (if v
      (:degree v)
      0)))

(defn- f-degree!
  [gbdb id-or-vertex f]
  (let [vertex (if (string? id-or-vertex)
                 (getv gbdb id-or-vertex)
                 id-or-vertex)]
    (if vertex
      (let [degree (:degree vertex)
            vertex (assoc vertex :degree (f degree))]
        (mysql/update! gbdb vertex)))))

(defn inc-degree!
  [gbdb id-or-vertex]
  (f-degree! gbdb id-or-vertex inc))

(defn dec-degree!
  [gbdb id-or-vertex]
  (f-degree! gbdb id-or-vertex dec))

(declare remove!)

(defn putv!
  ([gbdb vertex ctxt]
     (let [gvert (maps/local->global vertex)
           lvert (maps/global->local gvert ctxt)]
       (if (and (maps/edge? gvert)
                (maps/positive? gvert))
         (remove! gbdb (maps/negate-edge gvert) ctxt))
       (if (not (exists? gbdb lvert))
         (let [lvert (assoc lvert :ts (.getTime (Date.)))]
           (mysql/putv! gbdb lvert)
           (add-link-to-global! gbdb (:id gvert) (:id lvert))
           (case (:type lvert)
             :entity (if (and
                          (= (id/id->type (:id gvert)) :entity)
                          (> (id/count-parts (:id gvert)) 1))
                       (let [vid (:eid gvert)
                             sid (id/last-part (:id gvert))
                             rel (maps/id->vertex
                                  (str "(r/*can_mean " sid " " vid ")"))]
                         (putv! gbdb rel ctxt)))
             :edge (doseq [id (maps/ids lvert)]
                     (let [v (maps/id->vertex id)]
                       (putv! gbdb v ctxt)
                       (inc-degree! gbdb (:id v))))
             nil)
           vertex)
         vertex)))
  ([gbdb vertex]
     (putv! gbdb vertex nil)))

(defn putrel!
  [gbdb ids ctxt]
  (putv! gbdb
         (maps/id->edge
          (id/ids->id ids))
         ctxt))

(defn remrel!
  [gbdb ids ctxt]
  (remove! gbdb
           (maps/id->edge
            (id/ids->id ids))
           ctxt))

(defn exists-rel?
  [gbdb ids ctxt]
  (exists? gbdb
           (maps/global->local
            (maps/id->edge
             (id/ids->id ids))
            ctxt)))

(defn update!
  [gbdb vertex]
  (mysql/update! gbdb vertex))

(defn all-users
  [gbdb]
  (mysql/all-users gbdb))

(defn remove!
  [gbdb vertex ctxt]
  (let [gvert (maps/local->global vertex)
        lvert (maps/global->local gvert ctxt)]
    (mysql/remove! gbdb lvert)
    (remove-link-to-global! gbdb (:id gvert) (:id lvert))
    (if (and (maps/edge? lvert)
             (maps/positive? lvert))
      (do (putv! gbdb (maps/negate gvert) ctxt)
          (doseq [id (maps/ids lvert)]
            (dec-degree! gbdb (id/eid->id id)))))))

(defn replace!
  [gbdb edge old-eid new-eid ctxt]
  (let [old-eid (id/local->global old-eid)
        new-eid (id/local->global new-eid)]
    (if (and (maps/edge? edge)
             (id/eid? old-eid)
             (id/eid? new-eid)
             (not= old-eid new-eid))
      (let [ids (maps/ids (maps/local->global edge))
            ids (map #(if (= % old-eid) new-eid %)
                     ids)
            new-edge (maps/ids->edge ids (:score edge))]
        (remove! gbdb edge ctxt)
        (putv! gbdb new-edge ctxt)))))

(defn id->eid
  [gbdb id]
  (if (and (= (id/id->type id) :entity) (> (count (id/parts id)) 0))
    (let [eid (:eid (getv gbdb id))]
      (if eid eid
        (let [gid (id/local->global id)
              lid (mysql/first-alt gbdb gid)
              eid (if lid (:eid (getv gbdb lid)))]
          (if eid (id/local->global eid) id))))
    id))

(defn- patid->local-eid
  [gbdb patid ctxt]
  (if (= patid "*")
    patid
    (id/global->local (id->eid gbdb patid) ctxt)))

(defn- edge-with-ctxts
  [edge edges-maps]
  (loop [e edge
         em edges-maps]
    (if (empty? em)
      e
      (recur
       (let [emap (first em)]
         (if (some #{e} (:edges emap))
           (assoc e :ctxts (conj (:ctxts e) (:ctxt emap)))
           e))
       (rest em)))))

(defn- edges-with-ctxts
  [edges edges-maps]
  (map #(edge-with-ctxts % edges-maps) edges))

(defn f->edges
  [f ctxts]
  (let [edges-maps (map
                    #(hash-map
                      :ctxt %
                      :edges (map maps/local->global (f %)))
                    ctxts)
        edges (into #{} (apply clojure.set/union (map :edges edges-maps)))
        edges (filter
               #(not (some #{(:id (maps/negate %))}
                           (map :id edges)))
               edges)
        edges (filter maps/positive? edges)
        edges (edges-with-ctxts edges edges-maps)]
    edges))

(defn pattern->edges
  [gbdb pattern ctxts]
  (let [f (fn [ctxt]
            (mysql/pattern->edges
             gbdb
             (map #(patid->local-eid gbdb % ctxt) pattern)
             ctxt))]
    (f->edges f ctxts)))

(defn id->edges
  ([gbdb id ctxts]
     (let [f #(mysql/id->edges gbdb
                               (id->eid gbdb (id/global->local id %))
                               %)]
       (f->edges f ctxts)))
  ([gbdb id ctxts depth]
     (if (> depth 0)
       (let [edges (id->edges gbdb id ctxts)
             ids (set (flatten (map maps/participant-ids edges)))
             ids (map id/eid->id ids)
             ids (filter #(< (degree gbdb % ctxts) 9999) ids)
             next-edges (map #(id->edges gbdb % ctxts (dec depth))
                             ids)]
         (apply clojure.set/union (conj next-edges edges))))))

(defn vertex->edges
  [gbdb center ctxts]
    (id->edges gbdb (:id center) ctxts))

(defn edges->vertex-ids
  [edges]
  (set (flatten (map maps/ids edges))))

(defn replace-vertex!
  [gbdb old new ctxt ctxts]
  (putv! gbdb new ctxt)
  (let [edges (vertex->edges gbdb old ctxts)]
    (doseq [edge edges]
      (replace! gbdb
                edge
                (:eid old)
                (:eid new)
                ctxt))))

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

(defn add-ctxt-to-user!
  [gbdb user ctxt-id]
  (let [ctxts (user/user->ctxts user)]
    (if (not (some #{ctxt-id} ctxts))
      (let [ctxts (conj ctxts ctxt-id)
            ctxts-str (clojure.string/join " " ctxts)
            user (assoc user :ctxts ctxts-str)]
        (update! gbdb user)))))
