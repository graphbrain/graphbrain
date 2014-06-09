(ns graphbrain.braingenerators.pagereader
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.urlnode :as url]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.text :as text]
            [graphbrain.braingenerators.webtools :as webtools]
            [graphbrain.braingenerators.htmltools :as htmltools]
            [graphbrain.braingenerators.meat :as meat]
            [graphbrain.braingenerators.ngrams :as ngrams]
            [graphbrain.disambig.entityguesser :as eg]))

(def g (gb/gbdb))

(defn leaf?
  [ngrams-graph ngram-set ngram]
  (let [part-of (set (keys (:in (ngrams-graph ngram))))]
    (not (some part-of ngram-set))))

(defn ngram->entity
  [gbdb ngrams-graph ng-ent-map ngram ctxts]
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
                             (first comps)
                             ctxts)
                            (rest comps))))
            eid (if (empty? components)
                  (id/name+classes->eid ngram-str ["topic"])
                  (id/name+comps->eid ngram-str (map #(:eid (ng-ent-map %))
                                                     components)))]
        (assoc ng-ent-map ngram (eg/guess gbdb ngram-str eid ctxts)))))

(defn ngrams-graph->entities
  [gbdb ngrams-graph ctxts]
  (loop [ngs ngrams-graph
         ng-ent-map {}]
    (if (empty? ngs)
      ng-ent-map
      (recur (rest ngs)
             (ngram->entity
              gbdb
              ngrams-graph
              ng-ent-map
              (first (first ngs))
              ctxts)))))

(defn html->entities
  [gbdb html ctxts]
  (let [ngrams (ngrams/html->ngrams html)
        ngrams-graph (ngrams/ngrams->graph ngrams)
        ngrams (ngrams/sorted-ngrams (keys ngrams-graph))]
    (ngrams-graph->entities gbdb ngrams-graph ctxts)))

(defn url+html->edges
  [gbdb url-str html ctxts]
  (let [url-id (url/url->id url-str)
        entities (html->entities gbdb html ctxts)]
    (map #(maps/id->edge
           (id/ids->id ["r/has_topic" url-id (:eid (second %))])
           (:score (first %))) entities)))

(defn assoc-title!
  [gbdb url-str title user-id]
  (let [url-id (url/url->id url-str)
        title-node (text/text->vertex title)
        edge-id (id/ids->id ["r/+title" url-id (:id title-node)])]
    (gb/putv! gbdb title-node "c/web")
    (gb/putv! gbdb (maps/id->edge edge-id) "c/web")))

(defn bookmark!
  [gbdb url-str user-id]
  (let [url-id (url/url->id url-str)
        edge-id (id/ids->id ["r/bookmarked" user-id url-id])]
    (gb/putv! gbdb (maps/id->edge edge-id) user-id)))

(defn extract-knowledge!
  [gbdb url-str ctxts user-id]
  (let [html (webtools/slurp-url url-str)
        jsoup (htmltools/html->jsoup html)
        title (htmltools/jsoup->title jsoup)
        meat (meat/extract-meat html)
        edges (url+html->edges gbdb url-str meat ctxts)]
    (assoc-title! gbdb url-str title user-id)
    (doseq [edge edges] (gb/putv! gbdb edge user-id))
    (bookmark! gbdb url-str user-id)))
