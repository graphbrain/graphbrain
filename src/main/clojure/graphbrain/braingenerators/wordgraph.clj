(ns graphbrain.braingenerators.wordgraph
  (:use graphbrain.braingenerators.nlptools
        graphbrain.eco.word
        graphbrain.eco.words)
  (:require [clojure.math.combinatorics :as combo]))

(defn node-links
  [node]
  (let [links (:links node)]
    (if links links #{})))

(defn add-edge
  [graph pair]
  (let [orig (first pair)
        targ (second pair)
        orig-node (graph orig)
        targ-node (graph targ)
        orig-links (node-links orig-node)
        targ-links (node-links targ-node)
        g (assoc graph orig (assoc orig-node :links (conj orig-links targ)))
        g (assoc g targ (assoc targ-node :links (conj targ-links orig)))]
    g))

(defn- relevant?
  [word]
  (or (noun? word) (adjective? word)))

(defn- pair-relevant?
  [pair]
  (and (relevant? (first pair)) (relevant? (second pair))))

(defn- filter-and-add-edge
  [graph pair]
  (if (pair-relevant? pair)
    (add-edge graph pair)
    graph))

(defn- add-window-to-graph
  [window graph]
  (let [pairs (combo/combinations window 2)]
    (loop [g graph
           p pairs]
      (if (empty? p)
        g
        (recur (filter-and-add-edge g (first p))
               (rest p))))))

(defn words->graph
  [word-list window-size]
  (loop [graph {}
         windows (partition window-size 1 word-list)]
    (if (empty? windows)
      graph
      (recur (add-window-to-graph (first windows) graph)
             (rest windows)))))

(defn map-map-vals
  [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn add-field-all-nodes
  [graph field value]
  (map-map-vals #(assoc % field value) graph))

(defn init-pr
  [graph]
  (add-field-all-nodes graph :pr 1.0))

(defn- pr-contrib
  [graph node]
  (let [v (graph node)
        pr (:pr v)
        l (double (count (:links v)))]
    (/ pr l)))

(defn- new-node-pr
  [graph node damp]
  (let [links (:links node)
        prc (reduce + (map #(pr-contrib graph %) links))]
    (assoc node :newpr (+ (/ (- 1.0 damp) (double (count graph))) (* damp prc)))))

(defn- compute-new-pr
  [graph damp]
  (map-map-vals #(new-node-pr graph % damp) graph))

(defn- update-node-pr
  [node]
  (assoc node :pr (:newpr node)))

(defn- update-pr
  [graph]
  (map-map-vals update-node-pr graph))

(defn sorted-by-pr
  [graph]
  (into
   (sorted-map-by (fn [key1 key2]
                    (compare (:pr (graph key2)) (:pr (graph key1))))) graph))
(defn- pr-node-error
  [node]
  (let [nodev (second node)]
    (Math/abs (- (:pr nodev) (:newpr nodev)))))

(defn- pr-error
  [graph]
  (reduce max (map pr-node-error graph)))

(defn compute-pr
  [graph damp]
  (loop [g graph
         error 1]
    (if (< error 0.001)
      (sorted-by-pr g)
      (let [g1 (compute-new-pr g damp)
            err (pr-error g1)]
        (prn err)
        (recur (update-pr g1) err)))))

(defn print-top-words
  [graph]
  (let [top (take (quot (count graph) 3) graph)]
    (doseq [node top]
      (prn (:word (first node))))))

#_(def words (url->word-list "http://www.realclimate.org/index.php/archives/2014/03/the-nenana-ice-classic-and-climate/"))
