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
            [graphbrain.eco.eco :as eco]
            [graphbrain.eco.words :as words]
            [graphbrain.eco.parsers.header :as parser]
            [clojure.string :as str]))

(defn follow?
  [title]
  (not
   ((into #{} title) \:)))

(defn parse-link
  [link]
  (first
   (clojure.string/split link #"\|")))

(defn title->symbol
  [title]
  (if title
    (let [link (first
                (clojure.string/split title #"#"))]
      (if (not (empty? link))
        (sym/build
         [(sym/str->symbol link) "enwiki"])))))

(defn link-tuple
  [state link]
  (let [link* (title->symbol link)]
    (if (:cur-section state)
      [(:cur-section state) link*]
      [link*])))

(defn clean-header
  [header]
  (str/replace-first
   (str/replace-first header #"'*$" "")
   #"^'*" ""))

(defn- sentence->result
  [sentence]
  (let [env {:root "eco/1"
             :user "eco/1"}
        words (words/str->words sentence)
        par (eco/parse parser/header words env)
        vws (map eco/vert+weight par)
        res (eco/verts+weights->vertex parser/header vws env)]
    res))

(defn header->symbol
  [header]
  (if (and
       (> (count header) 0)
       (< (count header) 150))
    (do
      (println (str ">>>" header "<<<"))
      (println (sentence->result header))
      (sym/build
       [(sym/str->symbol header) "header"]))))

(defn parse-header
  [header]
  (let [header* (str/trim header)
        link (re-find #"\[\[([^\]]*)\]\]" header*)]
    (if link
      (title->symbol
       (parse-link (link 1)))
      (if header*
        (header->symbol
         (clean-header header*))))))

(defn parse-item
  [state item]
  (if (item 1)
    (assoc state :cur-section
           (parse-header (item 1)))
    (let [link (parse-link (item 2))]
      (if (follow? link)
        (assoc state :links
               (conj (:links state) (link-tuple state link)))
        state))))

(defn parse
  [title text]
  (reduce parse-item
          {:links #{}
           :cur-section nil}
          (re-seq #"==([^=]*)==|\[\[([^\]]*)\]\]" text)))

(defn with-links
  [revs title]
  (map
   #(dissoc (assoc %
              :links (:links
                      (parse title (:text %))))
            :text)
   revs))

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
           #(apply vector
                   (flatten
                    ["related/1" (title->symbol title) %]))
           (key rev))))

(defn with-beliefs
  [revs title]
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
      #_(doseq [r revs]
        (println r))
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

