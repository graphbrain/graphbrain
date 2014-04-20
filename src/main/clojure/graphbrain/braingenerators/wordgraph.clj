(ns graphbrain.braingenerators.wordgraph
  (:use graphbrain.utils
        graphbrain.graphtools
        graphbrain.pagerank
        graphbrain.braingenerators.nlptools
        graphbrain.eco.word
        graphbrain.eco.words)
  (:require [clojure.math.combinatorics :as combo]))


;; words -> word graph

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

(defn- words->graph
  [word-list]
  (loop [graph {}
         windows (partition 5 1 word-list)]
    (if (empty? windows)
      graph
      (recur (add-window-to-graph (first windows) graph)
             (rest windows)))))

(defn url->wordgraph
  [url-str]
  (words->graph (url->words url-str)))


;; compute pagerank on word graph

(defn graph->prgraph
  [graph]
  (let [g (init-pr graph)
        g (compute-pr g 0.85)] g))

(defn words->prgraph
  [words]
  (graph->prgraph (words->graph words)))

(defn url->prgraph
  [url-str]
  (graph->prgraph (url->wordgraph url-str)))


;; extract top words from pageranked graph

(defn- topwords
  [graph]
  (let [sorted-graph (sort-by :pr graph)
        top (take (quot (count sorted-graph) 3) sorted-graph)]
    (set (keys top))))

(defn prgraph->topwords
  [graph]
  (topwords graph))

(defn graph->topwordso
  [graph]
  (prgraph->topwords (graph->prgraph graph)))

(defn words->topwords
  [words]
  (prgraph->topwords (words->prgraph words)))

(defn url->topwords
  [url-str]
  (let [graph (url->prgraph url-str)]
    (prgraph->topwords graph)))

(defn print-topwords
  [url-str]
  (prn (map #(:word %) (url->topwords url-str))))


#_(def words (url->word-list "http://www.realclimate.org/index.php/archives/2014/03/the-nenana-ice-classic-and-climate/"))

