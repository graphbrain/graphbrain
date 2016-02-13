(ns graphbrain.kr.pagereader
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.hg.constants :as consts]
            [graphbrain.kr.webtools :as webtools]
            [graphbrain.kr.htmltools :as htmltools]
            [graphbrain.kr.nlptools :as nlp]
            [graphbrain.kr.wordgraph :as wg]
            [graphbrain.kr.meat :as meat]
            [graphbrain.kr.ngrams :as ngrams]
            [graphbrain.disambig.entityguesser :as eg]))

(def g (hgops/hg))

(defn leaf?
  [ngrams-graph ngram-set ngram]
  (let [part-of (set (keys (:in (ngrams-graph ngram))))]
    (not (some part-of ngram-set))))

(defn ngram->entity
  [hg ngrams-graph ng-ent-map ngram text ctxts]
  #_(if (ng-ent-map ngram) ng-ent-map
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
                             text
                             ctxts)
                            (rest comps))))
            eid (if (empty? components)
                  nil
                  (id/name+ids->eid consts/composed-eid-rel ngram-str
                                    (map #(:eid (ng-ent-map %))
                                         components)))]
        (assoc ng-ent-map ngram (eg/guess gbdb ngram-str text eid nil ctxts)))))

(defn ngrams-graph->entities
  [hg ngrams-graph text ctxts]
  (loop [ngs ngrams-graph
         ng-ent-map {}]
    (if (empty? ngs)
      ng-ent-map
      (recur (rest ngs)
             (ngram->entity
              hg
              ngrams-graph
              ng-ent-map
              (first (first ngs))
              text
              ctxts)))))


(defn ngram->text
  [ngram]
  (clojure.string/join " "
   (apply vector (map #(str (:word %) " " (:lemma %)) (:words ngram)))))

(defn ngrams->text
  [ngrams]
  (clojure.string/join " " (apply vector (map ngram->text ngrams))))

(defn prgraph->text
  [prgraph]
  (let [words (keys prgraph)]
    (clojure.string/join " "
                         (map #(str (:word %) " " (:lemma %)) words))))

(defn html->entities
  [hg html ctxts]
  (let [sentences (nlp/html->sentences html)
        prgraph (wg/words->prgraph (nlp/sentences->words sentences))
        twords (wg/prgraph->topwords prgraph)
        ngrams (ngrams/sorted-ngrams
                (ngrams/scored-ngrams
                 (ngrams/ngrams-with-pr
                  (ngrams/topwords->ngrams twords sentences) prgraph)))
        text (prgraph->text prgraph)
        ngrams-graph (ngrams/ngrams->graph ngrams)
        ngrams (ngrams/sorted-ngrams (keys ngrams-graph))]
    (ngrams-graph->entities hg ngrams-graph text ctxts)))

(defn url+html->edges
  [hg url-str html ctxts]
  #_(let [url-id (url/url->id url-str)
        entities (html->entities gbdb html ctxts)]
    (map #(maps/id->edge
           (id/ids->id ["r/has_topic" url-id (:eid (second %))])
           (:score (first %))) entities)))

(defn assoc-title!
  [hg url-str title user-id ctxt]
  #_(let [url-id (url/url->id url-str)
        title-node (text/text->vertex title)
        edge-id (id/ids->id ["r/*title" url-id (:id title-node)])]
    (gb/putv! gbdb title-node ctxt)
    (gb/putv! gbdb (maps/id->edge edge-id) ctxt)))

(defn bookmark!
  [hg url-str user-id ctxt]
  #_(let [url-id (url/url->id url-str)
        edge-id (id/ids->id ["r/bookmarked" user-id url-id])]
    (gb/putv! gbdb (maps/id->edge edge-id) ctxt)))

(defn extract-knowledge!
  [hg url-str ctxt ctxts user-id]
  #_(let [html (webtools/slurp-url url-str)
        jsoup (htmltools/html->jsoup html)
        title (htmltools/jsoup->title jsoup)
        meat (meat/extract-meat html)
        edges (url+html->edges gbdb url-str meat ctxts)]
    (assoc-title! gbdb url-str title user-id ctxt)
    (doseq [edge edges] (gb/putv! gbdb edge ctxt))
    (bookmark! gbdb url-str user-id ctxt)))
