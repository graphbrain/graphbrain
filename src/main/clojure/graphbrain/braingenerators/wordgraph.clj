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
  (words->graph (url->word-list url-str)))


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


;; build co-occurence topwords graph

(defn- add-pair-to-topword-graph
  [pair graph tw]
  (let [w1 (first pair)
        w2 (second pair)
        tw1 (contains? tw w1)
        tw2 (contains? tw w2)]
    (if (and tw1 tw2)
      (add-arc graph pair)
      graph)))

(defn- inc-occurrences
  [word graph tw]
  (if (contains? tw word)
    (inc-field graph word :occ)
    graph))

(defn- topwords->graph
  [word-list tw]
  (loop [graph {}
         windows (partition 2 1 word-list)
         orphan  (first (first windows))]
    (if (empty? windows)
      (inc-occurrences orphan graph tw)
      (let [pair (first windows)
            word (second pair)]
        (recur (inc-occurrences word
                (add-pair-to-topword-graph pair graph tw) tw)
              (rest windows) orphan)))))

(defn url->topwords-graph
  [url-str]
  (let [words (url->word-list url-str)]
    (topwords->graph words (words->topwords words))))


;; build ngrams

(defn- add-ngrams
  [graph ngrams node-id node-val ngram]
  (let [new-ngram (conj ngram node-id)
        in-arcs (keys (:in node-val))]
    (if (empty? in-arcs)
      (conj ngrams new-ngram)
      (loop [ngs ngrams
             inputs in-arcs]
        (if (empty? inputs)
          ngs
          (recur (add-ngrams graph ngs
                             (first inputs)
                             (graph (first inputs)) new-ngram)
                 (rest inputs)))))))

(defn topwords-graph->ngrams
  [twgraph]
  (loop [graph twgraph
         ngrams []]
    (if (empty? graph) ngrams
        (let [node (first graph)
              node-id (first node)
              node-val (second node)
              out-arcs (:out node-val)]
          (recur (rest graph)
                 (if (empty? out-arcs)
                   (add-ngrams twgraph ngrams node-id node-val nil)
                   ngrams))))))

(defn url->ngrams
  [url-str]
  (topwords-graph->ngrams (url->topwords-graph url-str)))

(defn print-ngrams
  [url-str]
  (let [ngrams (url->ngrams url-str)]
    (doseq [ngram ngrams]
      (prn (clojure.string/join " " (map #(:word %) ngram))))))

#_(def words (url->word-list "http://www.realclimate.org/index.php/archives/2014/03/the-nenana-ice-classic-and-climate/"))
