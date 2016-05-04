;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

(ns graphbrain.kr.pagereader
  (:require [graphbrain.hg.connection :as conn]
            [graphbrain.hg.beliefs :as beliefs]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.hg.constants :as consts]
            [graphbrain.kr.webtools :as webtools]
            [graphbrain.kr.htmltools :as htmltools]
            [graphbrain.kr.nlptools :as nlp]
            [graphbrain.kr.wordgraph :as wg]
            [graphbrain.kr.meat :as meat]
            [graphbrain.kr.ngrams :as ngrams]
            [graphbrain.disambig.entityguesser :as eg]))

(defn leaf?
  [ngrams-graph ngram-set ngram]
  (let [part-of (set (keys (:in (ngrams-graph ngram))))]
    (not (some part-of ngram-set))))

(defn ngram->entity
  [hg ngrams-graph ng-ent-map ngram text]
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
                             hg
                             ngrams-graph
                             nem
                             (first comps)
                             text)
                            (rest comps))))]
        (assoc ng-ent-map ngram
               (eg/guess hg ngram-str text)))))

(defn ngrams-graph->entities
  [hg ngrams-graph text]
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
              text)))))


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

(defn html->ngrams
  [hg html]
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
    ngrams))

(defn html->entities
  [hg html]
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
    ;; TODO: BUG?? What about ngrams?
    (ngrams-graph->entities hg ngrams-graph text)))

(defn url+html->edges
  [hg url-str html]
  (map #(vector "has_topic/1" url-str (second %))
       (html->entities hg html)))

(defn assoc-title!
  [hg url-str title]
  #_(let [url-id (url/url->id url-str)
          title-node (text/text->vertex title)
          edge ["title/1" url-id (:id title-node)]]
    (text/add! hg title-node)
    (beliefs/add! hg "pagereader/1" edge)))

(defn extract-knowledge!
  [hg url-str]
  (let [html (webtools/slurp-url url-str)
        jsoup (htmltools/html->jsoup html)
        title (htmltools/jsoup->title jsoup)
        meat (meat/extract-meat html)
        edges (url+html->edges hg url-str meat)]
    (assoc-title! hg url-str title)
    (doseq [edge edges]
      (beliefs/add! hg "pagereader/1" edge))))

(defn ngrams->str
  [ngrams]
  (clojure.string/join
   " " (map :word (:words ngrams))))

(defn print-topics!
  [hg url-str]
  (let [html (webtools/slurp-url url-str)
        jsoup (htmltools/html->jsoup html)
        title (htmltools/jsoup->title jsoup)
        meat (meat/extract-meat html)
        ngrams (reverse (html->ngrams hg meat))
        edges (url+html->edges hg url-str meat)]
    (doseq [ngram ngrams]
      (println (str (ngrams->str ngram) " (" (:score ngram) ")")))
    (doseq [edge edges]
      (println edge))))
