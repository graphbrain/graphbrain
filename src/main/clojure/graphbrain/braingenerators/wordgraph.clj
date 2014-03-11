(ns graphbrain.braingenerators.wordgraph
  (:use graphbrain.utils
        graphbrain.graphtools
        graphbrain.pagerank
        graphbrain.braingenerators.nlptools
        graphbrain.eco.word
        graphbrain.eco.words)
  (:require [clojure.math.combinatorics :as combo]))

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

(defn print-top-words
  [graph]
  (let [top (take (quot (count graph) 3) graph)]
    (doseq [node top]
      (prn (:word (first node))))))

(defn url->topwords
  [url-str]
  (let [words (url->word-list url-str)
        graph (words->graph words 10)
        graph (init-pr graph)
        graph (compute-pr graph 0.85)]
       (print-top-words graph)))

#_(def words (url->word-list "http://www.realclimate.org/index.php/archives/2014/03/the-nenana-ice-classic-and-climate/"))
