(ns graphbrain.braingenerators.pagereader
  (:use graphbrain.braingenerators.ngrams
        graphbrain.disambig.entityguesser
        graphbrain.db.graph))


(def g (graph))

(defn ngram->entity
  [graph ngram]
  (let [ngram-str (ngram->str ngram)]
    (prn ngram-str)
    (guess graph ngram-str "xpto")))

(defn ngrams->entities
  [graph ngrams]
  (map #(ngram->entity graph %) ngrams))

(defn print-leaf-entities
  [url-str]
  (let [ngrams (url->ngrams url-str)
        ngrams-graph (ngrams->graph ngrams)
        leafs (sorted-ngrams (leaf-ngrams ngrams-graph))
        entities (ngrams->entities g leafs)]
    (doseq [e entities]
      (prn e))))
