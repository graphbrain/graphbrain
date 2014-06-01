(ns graphbrain.braingenerators.pagereader
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.urlnode :as url]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.braingenerators.ngrams :as ngrams]
            [graphbrain.disambig.entityguesser :as eg]))

(def g (gb/gbdb))

(defn leaf?
  [ngrams-graph ngram-set ngram]
  (let [part-of (set (keys (:in (ngrams-graph ngram))))]
    (not (some part-of ngram-set))))

(defn ngram->entity
  [gbdb ngrams-graph ng-ent-map ngram]
  (if (ng-ent-map ngram) ng-ent-map
      (let [ngram-str (ngrams/ngram->str ngram) 
            components (keys (:out (ngrams-graph ngram)))
            components (filter #(leaf? ngrams-graph components %) components)
            ng-ent-map (loop [nem ng-ent-map
                              comps components]
                         (if (empty? comps)
                           nem
                           (recur
                            (ngram->entity
                             gbdb
                             ngrams-graph
                             nem
                             (first comps))
                            (rest comps))))
            eid (if (empty? components)
                  (id/name+classes->eid ngram-str ["topic"])
                  (id/name+comps->eid ngram-str (map #(:eid (ng-ent-map %))
                                                     components)))]
        (assoc ng-ent-map ngram (eg/guess gbdb ngram-str eid)))))

(defn ngrams-graph->entities
  [gbdb ngrams-graph]
  (loop [ngs ngrams-graph
         ng-ent-map {}]
    (if (empty? ngs)
      ng-ent-map
      (recur (rest ngs)
             (ngram->entity
              gbdb
              ngrams-graph
              ng-ent-map
              (first (first ngs)))))))

(defn url->entities
  [gbdb url-str]
  (let [ngrams (ngrams/url->ngrams url-str)
        ngrams-graph (ngrams/ngrams->graph ngrams)
        ngrams (ngrams/sorted-ngrams (keys ngrams-graph))]
    (ngrams-graph->entities gbdb ngrams-graph)))

(defn print-entities!
  [gbdb url-str]
  (let [entities (url->entities gbdb url-str)]
    (doseq [e entities]
      (prn e))))

(defn url->edges
  [gbdb url-str]
  (let [url-id (url/url->id url-str)
        entities (url->entities gbdb url-str)]
    (map #(maps/id->edge
           (id/ids->id ["r/has_topic" url-id (:eid (second %))])
           (:score (first %))) entities)))

(defn extract-knowledge!
  [gbdb url-str]
  (let [edges (url->edges gbdb url-str)]
    (doseq [edge edges] (gb/putv! gbdb edge))))
