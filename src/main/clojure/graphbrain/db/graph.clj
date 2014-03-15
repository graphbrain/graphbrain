(ns graphbrain.db.graph
  (:import (com.graphbrain.db Graph
                              VertexType
                              EntityNode
                              Edge
                              EdgeType
                              URLNode
                              UserNode
                              ProgNode
                              TextNode)))

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
   :pwdhash (. obj getPwdHash)
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

(defn vertex-obj->map
  [obj]
  (case (VertexType/getType (. obj getId))
    Entity (entity-obj->map obj)
    Edge (edge-obj->map obj)
    EdgeType (edge-type-obj->map obj)
    URL (url-obj->map obj)
    User (user-obj->map obj)
    Prog (prog-obj->map obj)
    Text (text-obj->map obj)))

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
  ([name] (new Graph name)))

(defn get-vert
  [graph id]
  (let [obj (. graph get id)]
    (if obj (vertex-obj->map obj))))

(defn put-vert!
  [graph vert]
  (vertex-obj->map
   (. graph put (map->vertex-obj vert))))
