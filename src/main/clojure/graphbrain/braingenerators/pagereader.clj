(ns graphbrain.braingenerators.pagereader
  (:use graphbrain.braingenerators.ngrams
        graphbrain.disambig.entityguesser)
  (:require [graphbrain.db.graph :as graph]
            [graphbrain.db.urlnode :as url]
            [graphbrain.db.edge :as edge]))

(defonce g (graph/graph))

(defn ngram->entity
  [graph ngram]
  (let [ngram-str (ngram->str ngram)]
    (prn ngram-str)
    (guess graph ngram-str "xpto")))

(defn ngrams->entities
  [graph ngrams]
  (map #(ngram->entity graph %) ngrams))

(defn url->leaf-entities
  [url-str]
  (let [ngrams (url->ngrams url-str)
        ngrams-graph (ngrams->graph ngrams)
        leafs (sorted-ngrams (leaf-ngrams ngrams-graph))]
    (ngrams->entities g leafs)))

(defn print-leaf-entities
  [url-str]
  (let [entities (url->leaf-entities url-str)]
    (doseq [e entities]
      (prn e))))

(defn url->edges
  [url-str]
  (let [url-id (url/url->id url-str)
        entities (url->leaf-entities url-str)]
    (map #(edge/ids->edge-id ["r/has_topic" url-id (:id %)]) entities)))

(defn extract-knowledge!
  [url-str]
  (let [edges (url->edges url-str)]
    (doseq [edge edges] (graph/putv! g edge))))
