(ns graphbrain.web.snodes
  (:require [graphbrain.web.common :as common]
            [graphbrain.web.visualvert :as vv]
            [graphbrain.web.extrasnodes :as xs]
            [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]))

(def ^:const max-snodes 15)

(defn- hide?
  [edge]
  #_(let [et (maps/edge-type edge)]
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

(defn- edge->visual
  [edge root-id]
  #_(let [c (count (maps/participant-ids edge))]
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
  #_(if (empty? edge-type)
    ""
    (let [rel-label (edgetype/label edge-type)
          text (node-label root-node)]
      (if (= rpos 0)
        (str rel-label " " text)
        (str text " " rel-label)))))

(defn- node->map
  [hg node-id edge root-id ctxt ctxts]
  (let [vv (vv/id->visual hg node-id ctxt ctxts)
        node-edge (:id (:parent edge))
        score (:score edge)
        ectxts (:ctxts edge)]
    (assoc vv
      :edge node-edge
      :edge-text (vv/edge-id->text (:id (:parent edge))
                                   ctxt)
      :score score
      :ctxts ectxts)))

(defn- se->node-id
  [se rp]
  (if (= (second rp) 0) (:id1 se) (:id2 se)))

(defn- snode
  [hg rp sedges root-node ctxt ctxts]
  (let [etype (first rp)
        rpos (second rp)
        label (link-label etype rpos root-node)
        nodes (map #(node->map hg
                               (se->node-id % rp)
                               %
                               (:id root-node)
                               ctxt ctxts) sedges)]
    {:nodes nodes
     :etype etype
     :rpos rpos
     :label label}))

(defn- snode-map
  [hg en-map root-node ctxt ctxts]
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
                   (snode hg rp (second en) root-node ctxt ctxts)))))))

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

(defn- edges->visual
  [root-id edges]
  #_(filter (complement nil?)
          (map #(edge->visual % root-id)
               (filter maps/positive? edges))))

(defn generate
  [hg root-id ctxt ctxts]
  #_(let [root-id (->> root-id
                     (gb/id->eid gbdb)
                     (id/local->global))
        edges (hide-edges
               (gb/id->edges gbdb root-id ctxts))
        visual-edges (edges->visual root-id edges)
        en-map (edge-node-map visual-edges root-id)
        root-node (vv/id->visual gbdb root-id ctxt ctxts)]
    (xs/extrasnodes gbdb root-id ctxt ctxts
                    (snodes-limit-size
                     (snode-map gbdb en-map root-node ctxt ctxts)))))
