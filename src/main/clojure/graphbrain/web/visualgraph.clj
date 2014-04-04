(ns graphbrain.web.visualgraph
  (:require [clojure.data.json :as json])
  (:import (com.graphbrain.db ID EdgeType VertexType EntityNode Vertex)
           (com.graphbrain.web Colors EdgeLabelTable)))

(def ^:const max-snodes 15)

(defn snode-id
  [rel-pos]
  (str (-> (first rel-pos)
           (.replace "/" "_")
           (.replace " " "_")
           (.replace "." "_")
           (.replace "+" "_"))
       "_" (second rel-pos)))

(defn hyper->edge
  [edge root-id]
  (if (> (count (. edge getParticipantIds)) 2)
    (if (= (.getEdgeType edge) "r/1/instance_of~owned_by")
      (if (= (nth (.getParticipantIds edge) 0) root-id)
        {:edge-type "r/1/has"
         :id1 (nth (.getParticipantIds edge) 2)
         :id2 root-id
         :parent edge}
        (if (= (nth (.getParticipantIds edge) 2) root-id)
          {:edge-type "r/1/has"
           :id1 root-id
           :id2 (nth (.getParticipantIds edge) 0)
           :parent edge}
          nil))
      (if (= (.getEdgeType edge) "r/1/has~of_type")
        {:edge-type (str "has " (ID/lastPart (nth (.getParticipantIds edge) 2)))
         :id1 (nth (.getParticipantIds edge) 0)
         :id2 (nth (.getParticipantIds edge) 1)
         :parent edge}
        (let [parts (.split (.getEdgeType edge) "~")]
          {:edge-type (str (first parts)
                           " "
                           (ID/lastPart (nth (.getParticipantIds edge) 1))
                           " "
                           (clojure.string/join " " (rest parts)))
           :id1 (nth (.getParticipantIds edge) 0)
           :id2 (nth (.getParticipantIds edge) 2)
           :parent edge})))
    {:edge-type (nth (.getIds edge) 0)
     :id1 (nth (.getIds edge) 1)
     :id2 (nth (.getIds edge) 2)
     :parent edge}))

(defn add-to-edge-node-map
  [en-map key e root-id]
  (if (or (and (= (second key) 0) (= (:id1 e) root-id))
          (and (= (second key) 1) (= (:id2 e) root-id)))
    en-map
    (assoc en-map key (conj (en-map key) e))))

(defn add-edge-to-edge-node-map
  [en-map edge root-id]
  (let [enm (add-to-edge-node-map en-map [(:edge-type edge) 0] edge root-id)
        enm (add-to-edge-node-map enm [(:edge-type edge) 1] edge root-id)]
    enm))

(defn edge-node-map
  [edges root-id]
  (reduce #(add-edge-to-edge-node-map %1 %2 root-id) {} edges))

(defn link-color
  [label]
  (nth Colors/colors (mod (Math/abs (.hashCode label)) (count Colors/colors))))

(defn fix-label
  [label]
  (if (.containsKey EdgeLabelTable/lt label)
    (.get EdgeLabelTable/lt label)
    label))

(defn link-label
  [edge-type]
  (if (empty? edge-type) "" (fix-label (EdgeType/label edge-type))))

(defn node->map
  [graph node-id node-edge root-id]
  (let [vtype (VertexType/getType node-id)
        node (if (and (= node-id root-id) (= vtype VertexType/Entity))
               (EntityNode. node-id)
               (.get graph node-id))
        node (if (nil? node) (Vertex/fromId node-id) node)]

    (condp = (. node type)
      (VertexType/Entity) {:id (.id node)
                           :type "text"
                           :text (.description graph node)
                           :edge node-edge}
      (VertexType/URL) {:id (.id node)
                        :type "url"
                        :text (if (empty? (.getTitle node))
                                (.getUrl node)
                                (.getTitle node))
                        :url (.getUrl node)
                        :icon (.getIcon node)
                        :edge node-edge}
      (VertexType/User) {:id (.id node)
                         :type "user"
                         :text (.getName node)
                         :edge node-edge}
      {:id (.id node)
       :type "text"
       :text (.id node)
       :edge node-edge})))

(defn se->node-id
  [se rp]
  (if (= (second rp) 0) (:id1 se) (:id2 se)))

(defn snode
  [graph rp sedges root-id]
  (let [label (link-label (first rp))
        color (link-color label)
        nodes (map #(node->map graph
                               (se->node-id % rp)
                               (get-in % [:parent :id])
                               root-id) sedges)]
    {:nodes nodes
     :etype (first rp)
     :rpos (second rp)
     :label label
     :color color}))

(defn snode-map
  [graph en-map root-id]
  (loop [enm en-map
         count 0
         snode-map {}]
    (if (or (> count max-snodes) (empty? enm)) snode-map
        (let [en (first enm)
              rp (first en)
              snid (snode-id rp)]
          (recur (rest enm) (inc count)
                 (assoc snode-map snid (snode graph rp (second en) root-id)))))))

(defn generate
  [graph root-id user]
  (let [user-id (if user (.id user) "")
        hyper-edges (.edges graph root-id user-id)
        visual-edges (filter (complement nil?)
                             (map #(hyper->edge % root-id)
                                  (filter #(. % isPositive) hyper-edges)))
        edge-node-map (edge-node-map visual-edges root-id)
        all-relations (into [] (for [[rp v] edge-node-map]
                                 {:rel (first rp)
                                      :pos (second rp)
                                      :label (link-label (first rp))
                                      :snode (snode-id rp)}))
        snode-map (snode-map graph edge-node-map root-id)
        snode-map (assoc snode-map :root
                         {:nodes [(node->map graph root-id "" root-id)]})]
    (json/write-str {:user user-id
                     :snodes snode-map
                     :allrelations all-relations})))
