(ns graphbrain.web.visualgraph
  (:require [clojure.data.json :as json]
            [graphbrain.web.colors :as colors]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.id :as id]
            [graphbrain.db.edgetype :as edgetype]
            [graphbrain.db.entity :as entity]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.urlnode :as url])
  (:import (com.graphbrain.web EdgeLabelTable)))

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
  (if (> (count (maps/participant-ids edge)) 2)
    (let [parts (.split (maps/edge-type edge) "~")]
      {:edge-type (str (first parts)
                       " "
                       (id/last-part (nth (maps/participant-ids edge) 1))
                       " "
                       (clojure.string/join " " (rest parts)))
       :id1 (nth (maps/participant-ids edge) 0)
       :id2 (nth (maps/participant-ids edge) 2)
       :score (:score edge)
       :parent edge})
    {:edge-type (nth (maps/ids edge) 0)
     :id1 (nth (maps/ids edge) 1)
     :id2 (nth (maps/ids edge) 2)
     :score (:score edge)
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
  (nth colors/colors (mod (Math/abs (.hashCode label)) (count colors/colors))))

(defn fix-label
  [label]
  (if (.containsKey EdgeLabelTable/lt label)
    (.get EdgeLabelTable/lt label)
    label))

(defn link-label
  [edge-type]
  (if (empty? edge-type) "" (fix-label (edgetype/label edge-type))))

(defn node->map
  [gbdb node-id edge root-id]
  (let [vtype (id/id->type node-id)
        node (if (= vtype :entity)
               (maps/id->vertex node-id)
               (gb/getv gbdb node-id))
        node (if (nil? node) (maps/id->vertex node-id) node)
        node-edge (:id (:parent edge))
        score (:score edge)]
    (condp = (:type node)
      :entity {:id (:id node)
               :type "text"
               :text (entity/description node)
               :edge node-edge
               :score score}
      :url {:id (:id node)
            :type "url"
            :text (if (empty? (:title node))
                    (url/url node)
                    (:title node))
            :url (:url node)
            :icon (:icon node)
            :edge node-edge
            :score score}
      :user {:id (:id node)
             :type "user"
             :text (:name node)
             :edge node-edge
             :score score}
      {:id (:id node)
       :type "text"
       :text (:id node)
       :edge node-edge
       :score score})))

(defn se->node-id
  [se rp]
  (if (= (second rp) 0) (:id1 se) (:id2 se)))

(defn snode
  [gbdb rp sedges root-id]
  (let [label (link-label (first rp))
        color (link-color label)
        nodes (map #(node->map gbdb
                               (se->node-id % rp)
                               %
                               root-id) sedges)]
    {:nodes nodes
     :etype (first rp)
     :rpos (second rp)
     :label label
     :color color}))

(defn snode-map
  [gbdb en-map root-id]
  (loop [enm en-map
         count 0
         snode-map {}]
    (if (or (> count max-snodes) (empty? enm)) snode-map
        (let [en (first enm)
              rp (first en)
              snid (snode-id rp)]
          (recur (rest enm) (inc count)
                 (assoc snode-map snid (snode gbdb rp (second en) root-id)))))))

(defn- snode-limit-size
  [snode]
  (let [max-nodes 10
        nodes (:nodes snode)]
    (if (> (count nodes) max-nodes)
      (let [nodes (sort-by :score nodes)
            nodes (take-last max-nodes nodes)]
        (assoc snode :nodes nodes))
      snode)))

(defn- snodes-limit-size
  [snodes]
  (into {} (for [[k v] snodes]
             [k (snode-limit-size v)])))

(defn generate
  [gbdb root-id user]
  (let [user-id (if user (:id user) "")
        root-id (gb/id->eid gbdb root-id)
        hyper-edges (gb/id->edges gbdb root-id user-id)
        visual-edges (filter (complement nil?)
                             (map #(hyper->edge % root-id)
                                  (filter maps/positive? hyper-edges)))
        edge-node-map (edge-node-map visual-edges root-id)
        all-relations (into [] (for [[rp v] edge-node-map]
                                 {:rel (first rp)
                                  :pos (second rp)
                                  :label (link-label (first rp))
                                  :snode (snode-id rp)}))
        snode-map (snode-map gbdb edge-node-map root-id)
        snode-map (assoc snode-map :root
                         {:nodes [(node->map gbdb root-id "" root-id)]})
        snode-map (snodes-limit-size snode-map)]
    (json/write-str {:user user-id
                     :snodes snode-map
                     :allrelations all-relations})))
