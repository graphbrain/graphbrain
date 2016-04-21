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

(ns graphbrain.kr.wikipedia
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.beliefs :as beliefs]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.hg.edge :as edge]
            [graphbrain.hg.constants :as const]
            [graphbrain.hg.beliefs :as beliefs]
            [clojure.string :as str])
  (:import (org.sweble.wikitext.engine.utils DefaultConfigEnWp)
           (org.sweble.wikitext.engine WtEngineImpl)
           (org.sweble.wikitext.engine PageTitle)
           (org.sweble.wikitext.engine PageId)
           (org.graphbrain.kr WikiTextConverter)))

(defn follow?
  [title]
  (not
   ((into #{} title) \:)))

(defn parse-link
  [link]
  (first
   (clojure.string/split link #"\|")))

(defn parse2
  [title text]
  {:links (into #{}
                (map parse-link
                     (filter follow?
                             (map #(% 1) (re-seq #"\[\[([^\]]*)\]\]" text)))))})

(defn parse3
  [title text]
  (doseq [item (map #(% 1) (re-seq #"==([^=]*)==|\[\[([^\]]*)\]\]" text))]
    (println item))
  {:links #{}})


(defn parse
  [title text]
  (try
    (let [config (DefaultConfigEnWp/generate)
          engine (WtEngineImpl. config)
          page-title (PageTitle/make config title)
          page-id (PageId. page-title -1)
          cp (.postprocess engine page-id text nil)
          p (WikiTextConverter. config)
          wiki-text (.go p (.getPage cp))
          links (into #{} (filter follow? (.getLinks p)))]
      {:links links})
    (catch Exception e
      (do
        (println "parsing failed!")
        {:links #{}}))))

(defn with-links
  [revs title]
  ;;(println "with-links...")
  (map
   #(dissoc (assoc %
              :links (:links
                      (parse3 title (:text %))))
            :text)
   revs))

(defn title->symbol
  [title]
  (if title
    (let [link (first
                (clojure.string/split title #"#"))]
      (if (not (empty? link))
        (sym/build
         [(sym/str->symbol link) "enwiki"])))))

(defn rev-with-link-changes
  [prev-rev rev]
  (let [prev-links (:links prev-rev)
        links (:links rev)
        new-links (clojure.set/difference links prev-links)
        lost-links (clojure.set/difference prev-links links)]
    (dissoc (assoc rev
              :new-links (into #{} new-links)
              :lost-links (into #{} lost-links))
            :links)))

(defn with-link-changes
  [revs]
  ;;(println "with-link-changes...")
  (loop [prev-rev {}
         rev (first revs)
         rest-revs (rest revs)
         result []]
    (if (nil? rev)
      result
      (recur rev
             (first rest-revs)
             (rest rest-revs)
             (conj result (rev-with-link-changes prev-rev rev))))))

(defn revision->beliefs
  [title rev key]
  (filter #(% 2)
          (map
           #(vector "related/1" (title->symbol title) (title->symbol %))
           (key rev))))

(defn with-beliefs
  [revs title]
  ;;(println "with-beliefs...")
  (map #(assoc %
          :new-beliefs (into #{} (revision->beliefs title % :new-links))
          :lost-beliefs (into #{} (revision->beliefs title % :lost-links)))
       revs))

(defn user->symbol
  [user]
  (if (nil? user)
    "anon/enwiki_usr_spec"
    (if #_(re-matches
         #"^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
         user)
        (= (count (filter #(= \. %) user)) 3)
      "anon/enwikiusr*"
      (sym/build
       [(sym/str->symbol user) "enwikiusr"]))))

(defn process-page!
  [hg page]
  (if-let [redirect (:redirect page)]
    (let [b [const/synonym (title->symbol (:title page)) (title->symbol redirect)]
          user "wiki/enwikiusr*"]
      (beliefs/add! hg user b))
    (let [title (:title page)
          title-sym (title->symbol title)
          revs (-> (:revisions page)
                   (with-links title)
                   with-link-changes
                   (with-beliefs title))]
      (ops/batch-exec!
       hg
       (flatten
        (map
         (fn [rev]
           (let [user (user->symbol (:user rev))
                 new-beliefs (:new-beliefs rev)
                 lost-beliefs (:lost-beliefs rev)]
             [#(beliefs/add! % "wiki/enwikiusr*" ["editor/1" user title-sym])
              (map #(fn [x] (beliefs/add! x user %)) new-beliefs)
              (map #(fn [x] (beliefs/remove! x user (edge/negative %))) new-beliefs)
              (map #(fn [x] (beliefs/add! x user (edge/negative %))) lost-beliefs)
              (map #(fn [x] (beliefs/remove! x user %)) lost-beliefs)]))
         revs))))))

