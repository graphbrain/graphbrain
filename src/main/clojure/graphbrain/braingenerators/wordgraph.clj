(ns graphbrain.braingenerators.wordgraph
  (:use graphbrain.braingenerators.nlptools)
  (:require [clojure.math.combinatorics :as combo]))

(defn add-edge
  [graph pair]
  (let [orig (first pair)
        targ (second pair)
        orig-node (graph orig)
        targ-node (graph targ)
        orig-links (:links orig-node)
        targ-links (:links targ-node)
        g (assoc graph orig (assoc orig-node :links (conj orig-links targ)))
        g (assoc g targ (assoc targ-node :links (conj targ-links orig)))]
    g))

(defn- add-window-to-graph
  [window graph]
  (let [pairs combo/combinations window 2]
    (loop [g graph
           p pairs]
      (if (empty? p)
        g
        (recur (add-edge g p)
               (rest p))))))

(defn word-list->graph
  [word-list window-size]
  (loop [graph {}
         windows (partition window-size 1 word-list)]
    (if (empty? windows)
      graph
      (recur (add-edge graph (first windows))
             (rest windows)))))

(def words (url->word-list "http://www.realclimate.org/index.php/archives/2014/03/the-nenana-ice-classic-and-climate/"))
