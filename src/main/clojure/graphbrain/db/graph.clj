(ns graphbrain.db.graph
  (:require [graphbrain.db.mysql :as mysql]
            [graphbrain.db.id :as id]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.edge :as edge]
            [graphbrain.db.user :as user])
  (:import (com.graphbrain.db VertexType
                              EntityNode
                              Edge
                              EdgeType
                              URLNode
                              UserNode
                              ProgNode
                              TextNode)
           (java.util Date)))

(defn entity-obj->map
  [obj]
  {:id (. obj getId)
   :type :entity
   :degree (. obj getDegree)
   :ts (. obj getTs)})

(defn edge-obj->map
  [obj]
  {:id (. obj getId)
   :type :edge
   :degree (. obj getDegree)
   :ts (. obj getTs)
   :elems (. obj getElems)
   :ids (. obj getIds)
   :edge-type (. obj getEdgeType)
   :participant-ids (. obj getParticipantIds)})

(defn edge-type-obj->map
  [obj]
  {:id (. obj getId)
   :type :edge-type
   :degree (. obj getDegree)
   :ts (. obj getTs)
   :label (. obj getLabel)})

(defn url-obj->map
  [obj]
  {:id (. obj getId)
   :type :url
   :degree (. obj getDegree)
   :ts (. obj getTs)
   :title (. obj getTitle)
   :icon (. obj getIcon)
   :url (. obj getUrl)})

(defn user-obj->map
  [obj]
  {:id (. obj getId)
   :type :user
   :degree (. obj getDegree)
   :ts (. obj getTs)
   :username (. obj getUsername)
   :name (. obj getName)
   :email (. obj getEmail)
   :pwdhash (. obj getPwdhash)
   :role (. obj getRole)
   :session (. obj getSession)
   :session-ts (. obj getSessionTs)
   :last-seen (. obj getLastSeen)})

(defn prog-obj->map
  [obj]
  {:id (. obj getId)
   :type :prog
   :degree (. obj getDegree)
   :ts (. obj getTs)
   :prog (. obj getProg)})

(defn text-obj->map
  [obj]
  {:id (. obj getId)
   :type :text
   :degree (. obj getDegree)
   :ts (. obj getTs)
   :text (. obj getText)})

(defn vertex-type
  [id]
  (condp = (VertexType/getType id)
    VertexType/Entity :entity
    VertexType/Edge :edge
    VertexType/EdgeType :edge-type
    VertexType/URL :url
    VertexType/User :user
    VertexType/Prog :prog
    VertexType/Text :text))

(defn vertex-obj->map
  [obj]
  (condp = (vertex-type (. obj getId))
    :entity (entity-obj->map obj)
    :edge (edge-obj->map obj)
    :edge-type (edge-type-obj->map obj)
    :url (url-obj->map obj)
    :user (user-obj->map obj)
    :prog (prog-obj->map obj)
    :text (text-obj->map obj)))

(defn map->entity-obj
  [m]
  (new EntityNode
       (:id m)
       (:degree m)
       (:ts m)))

(defn map->edge-obj
  [m]
  (new Edge
       (:elems m)
       (:degree m)
       (:ts m)))

(defn map->edge-type-obj
  [m]
  (new EdgeType
       (:id m)
       (:label m)
       (:degree m)
       (:ts m)))

(defn map->url-obj
  [m]
  (new URLNode
       (:id m)
       (:title m)
       (:icon m)
       (:degree m)
       (:ts m)))

(defn map->user-obj
  [m]
  (new UserNode
       (:id m)
       (:username m)
       (:name m)
       (:email m)
       (:pwdhash m)
       (:role m)
       (:session m)
       (:session-ts m)
       (:last-seen m)
       (:degree m)
       (:ts m)))

(defn map->prog-obj
  [m]
  (new ProgNode
       (:id m)
       (:prog m)
       (:degree m)
       (:ts m)))

(defn map->text-obj
  [m]
  (new TextNode
       (:id m)
       (:text m)
       (:degree m)
       (:ts m)))

(defn map->vertex-obj
  [m]
  (case (:type m)
    :entity (map->entity-obj m)
    :edge (map->edge-obj m)
    :edge-type (map->edge-type-obj m)
    :url (map->url-obj m)
    :user (map->user-obj m)
    :prog (map->prog-obj m)
    :text (map->text-obj m)))

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
  [graph global-id user-id]
  (mysql/add-link-to-global! graph global-id user-id))

(defn remove-link-to-global!
  [graph global-id user-id]
  (mysql/remove-link-to-global! graph global-id user-id))

(defn- f-degree!
  [graph id-or-vertex f]
  (let [vertex (if (string? id-or-vertex) (getv id-or-vertex) id-or-vertex)
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
         (mysql/putv! vertex)
         (if (= (:type vertex) :edge)
           (doseq [id (edge/ids vertex)]
             (putv! (vertex/id->vertex id))
             (inc-degree! id)))))
     vertex)
  ([graph vertex user-id]
     (putv! vertex)
     (if (not (id/user-space? (:id vertex)))
       (let [uvert (vertex/global->user vertex user-id)]
         (if (not (exists? uvert))
           (putv! uvert)
           (add-link-to-global! (:id vertex) (:id uvert)))
         #_(if (= (:type vertex) :edge)
            ;; run consensus algorithm
            (let [gedge (vertex/user->global vertex)]
              (consensus/eval-edge graph gedge)))))
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
  (= (:type vert) :edge))

(defn username->node
  [graph username]
  (getv graph (id/username->id username)))

(defn all-users
  [graph]
  (map vertex-obj->map (.allUsers graph)))

(defn id->edges
  [graph id user-id]
  (map edge-obj->map (.edges graph id user-id)))

(defn- on-remove-edge!
  [graph edge]
  (doseq [id (edge/ids edge)] (dec-degree! graph id)))

(defn remove!
  ([graph vertex]
     (mysql/remove! graph vertex)
     (if (= (:type vertex) :edge)
       (on-remove-edge! vertex)))
  ([graph vertex user-id]
     (let [u (vertex/global->user vertex user-id)]
       (remove graph u)
       (remove-link-to-global! (:id vertex) (:id u))
       (if (= (:type vertex) :edge)
         (do (putv! (edge/negate vertex))
             #_(consensus/eval-edge! graph vertex))))))

(defn pattern->edges
  [graph pattern]
  (mysql/pattern->edges graph pattern))

(defn id->edges
  ([graph center]
     (mysql/id->edges graph center))
  ([graph center-id user-id]
     (let [edges (id->edges graph center-id)
           gedges (filter vertex/global-space? edges)
           uedges (if user-id
                    (let [ucenter-id (id/global->user center-id user-id)]
                      (filter vertex/user-space? (id->edges graph ucenter-id))) #{})]
       (clojure.set/union (set (filter #(not (some #{(edge/negate %)} uedges))
                                       gedges))
                          (set (filter edge/positive? uedges))))))

(defn vertex->edges
  [graph center]
    (id->edges graph (:id center)))

(defn edges->vertex-ids
  [edges]
  (set (flatten (map edge/ids edges))))

(defn neighbors
  [grpah center-id]
  (conj (edges->vertex-ids (id->edges graph center-id)) center-id))

(defn description
  [graph id-or-vertex]
  (let [id (if (string? id-or-vertex) id-or-vertex (:id id-or-vertex))
        vertex (if (string? id-or-vertex) (getv graph id-or-vertex) id-or-vertex)
        as-in (pattern->edges graph (str "(r/+type_of " id " *)"))
        desc (vertex/label vertex)]
    (if (empty? as-in) desc
        (str desc " ("
             (clojure.string/join ", "
                                  (map #(vertex/label (second (edge/ids %))) as-in))
             ")"))))

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
  [login]
  (if (exists? (id/username->id login))
    (getv id/username->id login)
    (let [uid (email->id login)]
            (if uid
              (if (exists? uid) (getv uid))))))

(defn username->vertex
  [username]
  (let [uid (id/username->id username)]
    (if (exists? uid) (getv uid))))

(defn create-user!
  [username name email password role]
  (let [user {:username username
              :name name
              :email email
              :password password
              :role role}]
    (putv! user)))

(defn attempt-login!
  [login password]
  (let [user (find-user login)]
    (if (and user (user/check-password user password))
        (update! (user/new-session user)))))

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
  [graph vertex]
  (let [vertex (if (string? vertex) (getv graph vertex) vertex)
        as-in (pattern->edges graph ["r/+type_of" (:id vertex) "*"])
        desc (vertex/label vertex)]
    (if (empty? as-in) desc
        (str desc " ("
             (clojure.string/join
              ", "
              (map #(vertex/label (second (edge/participant-ids %))) as-in)) ")"))))
