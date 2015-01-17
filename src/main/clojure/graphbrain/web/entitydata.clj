(ns graphbrain.web.entitydata
  (:require [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.visualvert :as vv]
            [graphbrain.db.gbdb :as gb]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.id :as id]
            [graphbrain.db.edgetype :as edgetype]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.context :as context])
  (:import (com.graphbrain.web EdgeLabelTable)))

(def ^:const max-snodes 15)

(defn- hide?
  [edge]
  (let [et (maps/edge-type edge)]
    (or (= et "r/*title")
        (= et "r/*author"))))

(defn- hide-edges
  [edges]
  (filter #(not (hide? %)) edges))

(defn- snode-id
  [rel-pos]
  (str (-> (first rel-pos)
           (clojure.string/replace #"\W" "_"))
       "_" (second rel-pos)))

(defn- hyper->edge
  [edge root-id]
  (let [c (count (maps/participant-ids edge))]
    (cond
     (> c 2)
     (let [parts (.split (maps/edge-type edge) "~")]
       {:edge-type (str (first parts)
                        " "
                        (id/last-part (nth (maps/participant-ids edge) 1))
                        " "
                        (clojure.string/join " " (rest parts)))
        :id1 (nth (maps/participant-ids edge) 0)
        :id2 (nth (maps/participant-ids edge) 2)
        :score (:score edge)
        :ctxts (:ctxts edge)
        :parent edge})
     (= c 2)
     {:edge-type (nth (maps/ids edge) 0)
      :id1 (nth (maps/ids edge) 1)
      :id2 (nth (maps/ids edge) 2)
      :score (:score edge)
      :ctxts (:ctxts edge)
      :parent edge}
     :else nil)))

(defn- add-to-edge-node-map
  [en-map key e root-id]
  (if (or (and (= (second key) 0) (= (:id1 e) root-id))
          (and (= (second key) 1) (= (:id2 e) root-id)))
    en-map
    (assoc en-map key (conj (en-map key) e))))

(defn- add-edge-to-edge-node-map
  [en-map edge root-id]
  (let [enm (add-to-edge-node-map en-map [(:edge-type edge) 0] edge root-id)
        enm (add-to-edge-node-map enm [(:edge-type edge) 1] edge root-id)]
    enm))

(defn- edge-node-map
  [edges root-id]
  (reduce #(add-edge-to-edge-node-map %1 %2 root-id) {} edges))

(defn- fix-label
  [label]
  (if (.containsKey EdgeLabelTable/lt label)
    (.get EdgeLabelTable/lt label)
    label))

(defn- node-label
  [node]
  (case (:type node)
    :entity (:text node)
    :user (:text node)
    :context (:text node)
    :url "web page"
    (name (:type node))))

(defn- link-label
  [edge-type rpos root-node]
  (if (empty? edge-type)
    ""
    (let [rel-label (fix-label (edgetype/label edge-type))
          text (node-label root-node)]
      (if (= rpos 0)
        (str rel-label " " text)
        (str text " " rel-label)))))

(defn- node->map
  [gbdb node-id edge root-id ctxts]
  (let [vv (vv/id->visual gbdb node-id ctxts)
        node-edge (:id (:parent edge))
        score (:score edge)
        ectxts (:ctxts edge)]
    (assoc vv
      :edge node-edge
      :score score
      :ctxts ectxts)))

(defn- se->node-id
  [se rp]
  (if (= (second rp) 0) (:id1 se) (:id2 se)))

(defn- snode
  [gbdb rp sedges root-node ctxts]
  (let [etype (first rp)
        rpos (second rp)
        label (link-label etype rpos root-node)
        nodes (map #(node->map gbdb
                               (se->node-id % rp)
                               %
                               (:id root-node)
                               ctxts) sedges)]
    {:nodes nodes
     :etype etype
     :rpos rpos
     :label label}))

(defn- snode-map
  [gbdb en-map root-node ctxts]
  (loop [enm en-map
         count 0
         snode-map {}]
    (if (or (> count max-snodes) (empty? enm)) snode-map
        (let [en (first enm)
              rp (first en)
              snid (snode-id rp)]
          (recur (rest enm) (inc count)
                 (assoc snode-map
                   snid
                   (snode gbdb rp (second en) root-node ctxts)))))))

(defn- snode-limit-size
  [snode]
  (let [max-nodes 10
        nodes (:nodes snode)]
    (if (> (count nodes) max-nodes)
      (let [nodes (sort-by :score nodes)
            nodes (take-last max-nodes nodes)
            ]
        (assoc snode :nodes nodes))
      snode)))

(defn- snodes-limit-size
  [snodes]
  (into {} (for [[k v] snodes]
             [k (snode-limit-size v)])))

(defn snodes
  [gbdb root-id ctxts]
  (let [root-id (gb/id->eid gbdb root-id)
        root-id (id/local->global root-id)
        hyper-edges (gb/id->edges gbdb root-id ctxts)
        hyper-edges (hide-edges hyper-edges)
        visual-edges (filter (complement nil?)
                             (map #(hyper->edge % root-id)
                                  (filter maps/positive? hyper-edges)))
        edge-node-map (edge-node-map visual-edges root-id)
        root-node (node->map gbdb root-id "" root-id ctxts)
        all-relations (into [] (for [[rp v] edge-node-map]
                                 {:rel (first rp)
                                  :pos (second rp)
                                  :label (link-label
                                          (first rp)
                                          (second rp)
                                          root-node)
                                  :snode (snode-id rp)}))
        snode-map (snode-map gbdb edge-node-map root-node ctxts)
        snode-map (assoc snode-map :root
                         {:nodes [root-node]})
        snode-map (snodes-limit-size snode-map)]
    snode-map))

(defn generate
  [gbdb root-id user ctxts]
  (let [context-data (contexts/context-data root-id (:id user))
        user-id (if user (:id user) "")
        root-id (gb/id->eid gbdb root-id)
        root-id (id/local->global root-id)
        hyper-edges (gb/id->edges gbdb root-id ctxts)
        hyper-edges (hide-edges hyper-edges)
        visual-edges (filter (complement nil?)
                             (map #(hyper->edge % root-id)
                                  (filter maps/positive? hyper-edges)))
        edge-node-map (edge-node-map visual-edges root-id)
        root-node (node->map gbdb root-id "" root-id ctxts)
        snode-map (snode-map gbdb edge-node-map root-node ctxts)
        snode-map (snodes-limit-size snode-map)]
    {:root root-node
     :snodes snode-map
     :ctxts (zipmap ctxts
                    (map #(hash-map
                           :name (context/label %)
                           :color (contexts/color % user-id))
                         ctxts))
     :context context-data}))
