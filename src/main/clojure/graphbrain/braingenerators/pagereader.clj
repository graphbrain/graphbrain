(ns graphbrain.braingenerators.pagereader
  (:use graphbrain.braingenerators.ngrams
        graphbrain.disambig.entityguesser)
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.urlnode :as url]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]))

;;(def g (gb/gbdb))

(defn ngram->entity
  [gbdb ngram]
  (let [ngram-str (ngram->str ngram)]
    (prn ngram-str)
    (guess gbdb ngram-str "xpto")))

(defn ngrams->entities
  [gbdb ngrams]
  (map #(ngram->entity gbdb %) ngrams))

(defn url->leaf-entities
  [gbdb url-str]
  (let [ngrams (url->ngrams url-str)
        ngrams-graph (ngrams->graph ngrams)
        leafs (sorted-ngrams (leaf-ngrams ngrams-graph))]
    (ngrams->entities gbdb leafs)))

(defn print-leaf-entities
  [gbdb url-str]
  (let [entities (url->leaf-entities gbdb url-str)]
    (doseq [e entities]
      (prn e))))

(defn url->edges
  [gbdb url-str]
  (let [url-id (url/url->id url-str)
        entities (url->leaf-entities gbdb url-str)]
    (map #(id/ids->id ["r/has_topic" url-id (:id %)]) entities)))

(defn extract-knowledge!
  [gbdb url-str]
  (let [edges (url->edges url-str)]
    (doseq [edge-id edges] (gb/putv! gbdb (maps/id->edge edge-id)))))
