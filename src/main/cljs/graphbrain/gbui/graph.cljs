(ns graphbrain.gbui.graph
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.snode :as snode]
            [graphbrain.gbui.layout :as layout])
  (:use [jayq.core :only [$]]))

(defn snodes-vis-map
  [snodes]
  (into {}
         (map #(vector (first %) (snode/create-snode-vis)) snodes)))

(defn create-graph-vis-state
  [size snodes]
  (let [quat (js/Quaternion.)
        affin-mat (js/Array. 16)
        graph {:size size
               :scale 1
               :offset [0 0 0]
               :quat quat
               :delta-quat (js/Quaternion.)
               :affin-mat affin-mat
               :negative-stretch 1
               :mapping-power 1
               :snodes (snodes-vis-map snodes)}]
    (. quat getMatrix affin-mat)
    graph))

(defn graph-view-size
  []
  (let [graph-view ($ "#graph-view")
        width (jq/width graph-view)
        height (jq/height graph-view)]
    [width height]))

(defn update-size
  []
  (reset! g/graph-vis (assoc @g/graph-vis :size (graph-view-size))))

(defn update-transform
  []
  (let [gv @g/graph-vis
        offset (:offset gv)
        offset-x (first offset)
        offset-y (second offset)
        scale (:scale gv)
        transform-str (str "translate("
                           offset-x
                           "px,"
                           offset-y
                           "px)"
                           " scale("
                           scale
                           ")")
        gv-div ($ "#graph-view")]
    (jq/css gv-div {:-webkit-transform transform-str})
    (jq/css gv-div {:-moz-transform transform-str})))

(defn update-view
  []
  (doseq [snode-id (keys (:snodes @g/graph-vis))] (snode/apply-pos snode-id)))

(defn snodes->seq
  [snodes]
  (map #(assoc (second %) :id (first %)) snodes))

(defn seq->snodes
  [snodes]
  (into {} (map #(vector (:id %) (dissoc % :id)) snodes)))

(defn graph-layout
  []
  (snode/move-to "root" 0 0 0)
  (let [gv @g/graph-vis
        snodes (snodes->seq
                (filter #(not (snode/is-root (first %))) (:snodes gv)))
        ;;snodes (snodes->seq (:snodes gv))
        snodes (layout/layout snodes)]
    (let [negative-stretch 1
          mapping-power 1
          N (count snodes)
          Nt 7
          c (> N (* Nt 2))
          mapping-power (if c
                          (* (/ (Math/asin (/ Nt (/ N 2.0))) Math/PI)
                             (/ 1.0 (Math/log 0.5)))
                          mapping-power)
          negative-stretch (if c (* mapping-power 2.0) negative-stretch)
          gv (assoc gv :mapping-power mapping-power
                       :negative-stretch negative-stretch
                       :snodes (seq->snodes snodes))]
      (reset! g/graph-vis gv)))
    (update-view))

(defn init-graph-vis!
  []
  (reset! g/graph-vis (create-graph-vis-state
                       (graph-view-size)
                       (:snodes @g/graph)))
  (doseq [snode-id (keys (:snodes @g/graph-vis))]
    (snode/place snode-id))
  (graph-layout)
  (update-transform))

(defn- snode-ids->str
  [graph]
  (let [snodes (:snodes graph)
        snodes (into {}
                      (map
                       #(vector
                         (.substring (str (first %)) 1) (second %)) snodes))]
    (assoc graph :snodes snodes)))

(defn init-graph!
  []
  (reset! g/graph
          (snode-ids->str (js->clj js/data :keywordize-keys true)))
  (init-graph-vis!))

(defn rotate
  [x y z]
  (let [gv @g/graph-vis
        quat (:quat gv)
        delta-quat (:delta-quat gv)
        affin-mat (:affin-mat gv)]
    (. delta-quat fromEuler x y z)
    (. quat mul delta-quat)
    (. quat normalise)
    (. quat getMatrix affin-mat)))

(defn rotate-x
  [angle]
  (rotate angle 0 0))

(defn rotate-y
  [angle]
  (rotate 0 0 angle))

(defn zoom
  [delta-zoom x y]
  (let [gv @g/graph-vis
        scale (:scale gv)
        new-scale (+ scale (* 0.3 delta-zoom))
        new-scale (if (< new-scale 0.4) 0.4 new-scale)
        offset (:offset gv)
        offset-x (first offset)
        offset-y (second offset)
        offset-z (nth offset 2)]
    (if (>= delta-zoom 0)
      (let [size (:size gv)
            width (first size)
            height (second size)
            half-width (/ width 2.0)
            half-height (/ height 2.0)
            rx (- x half-width)
            ry (- y half-height)
            new-offset-x (- rx (* (/ (- rx offset-x) scale) new-scale))
            new-offset-y (- ry (* (/ (- ry offset-y) scale) new-scale))
            gv (assoc gv :offset [new-offset-x new-offset-y offset-z]
                         :scale new-scale)]
        (reset! g/graph-vis gv))
      (let [offset (if (> (- scale 0.4) 0)
                     (let [r (/ (- new-scale 0.4) (- scale 0.4))]
                       [(* offset-x r) (* offset-y r) offset-z])
                     offset)
            gv (assoc gv :offset offset :scale new-scale)]
        (reset! g/graph-vis gv))))
  (update-transform))
