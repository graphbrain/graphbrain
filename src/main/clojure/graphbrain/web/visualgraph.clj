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
    (if (= (maps/edge-type edge) "r/1/instance_of~owned_by")
      (if (= (nth (maps/participant-ids edge) 0) root-id)
        {:edge-type "r/1/has"
         :id1 (nth (maps/participant-ids edge) 2)
         :id2 root-id
         :parent edge}
        (if (= (nth (maps/participant-ids edge) 2) root-id)
          {:edge-type "r/1/has"
           :id1 root-id
           :id2 (nth (maps/participant-ids edge) 0)
           :parent edge}
          nil))
      (if (= (maps/edge-type edge) "r/1/has~of_type")
        {:edge-type (str "has " (id/last-part (nth (maps/participant-ids edge) 2)))
         :id1 (nth (maps/participant-ids edge) 0)
         :id2 (nth (maps/participant-ids edge) 1)
         :parent edge}
        (let [parts (.split (maps/edge-type edge) "~")]
          {:edge-type (str (first parts)
                           " "
                           (id/last-part (nth (maps/participant-ids edge) 1))
                           " "
                           (clojure.string/join " " (rest parts)))
           :id1 (nth (maps/participant-ids edge) 0)
           :id2 (nth (maps/participant-ids edge) 2)
           :parent edge})))
    {:edge-type (nth (maps/ids edge) 0)
     :id1 (nth (maps/ids edge) 1)
     :id2 (nth (maps/ids edge) 2)
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
  [gbdb node-id node-edge root-id]
  (let [vtype (id/id->type node-id)
        node (if (and (= node-id root-id) (= vtype :entity))
               (maps/id->vertex node-id)
               (gb/getv gbdb node-id))
        node (if (nil? node) (maps/id->vertex node-id) node)]
    (condp = (:type node)
      :entity {:id (:id node)
               :type "text"
               :text (gb/description gbdb node)
               :edge node-edge}
      :url {:id (:id node)
            :type "url"
            :text (if (empty? (:title node))
                    (url/url node)
                    (:title node))
            :url (:url node)
            :icon (:icon node)
            :edge node-edge}
      :user {:id (:id node)
             :type "user"
             :text (:name node)
             :edge node-edge}
      {:id (:id node)
       :type "text"
       :text (:id node)
       :edge node-edge})))

(defn se->node-id
  [se rp]
  (if (= (second rp) 0) (:id1 se) (:id2 se)))

(defn snode
  [gbdb rp sedges root-id]
  (let [label (link-label (first rp))
        color (link-color label)
        nodes (map #(node->map gbdb
                               (se->node-id % rp)
                               (:id (:parent %))
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

(defn generate
  [gbdb root-id user]
  (let [user-id (if user (:id user) "")
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
                         {:nodes [(node->map gbdb root-id "" root-id)]})]
    (json/write-str {:user user-id
                     :snodes snode-map
                     :allrelations all-relations})))
