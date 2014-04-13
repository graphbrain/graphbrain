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
  ([name] (new Graph name)))

(defn getv
  [graph id]
  (let [obj (. graph get id)]
    (if obj (vertex-obj->map obj))))

(defn putv!
  ([graph vert]
     (vertex-obj->map
      (. graph put (map->vertex-obj vert))))
  ([graph vert user-id]
     (vertex-obj->map
      (. graph put (map->vertex-obj vert)))))

(defn edge?
  [vert]
  (= (:type vert) :edge))

(defn username->node
  [graph username]
  (vertex-obj->map
   (.getUserNodeByUsername graph username)))

(defn all-users
  [graph]
  (map vertex-obj->map (.allUsers graph)))

(defn description
  [graph id]
  (.description graph id))

(defn id->edges
  [graph id user-id]
  (map edge-obj->map (.edges graph id user-id)))
