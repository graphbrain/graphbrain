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
  (:require [graphbrain.hg.beliefs :as beliefs]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.hg.constants :as const])
  (:import (org.sweble.wikitext.engine.utils DefaultConfigEnWp)
           (org.sweble.wikitext.engine WtEngineImpl)
           (org.sweble.wikitext.engine PageTitle)
           (org.sweble.wikitext.engine PageId)
           (org.graphbrain.kr WikiTextConverter)
           (org.wikipedia Wiki)))

(defn parse
  [title text]
  (let [config (DefaultConfigEnWp/generate)
        engine (WtEngineImpl. config)
        page-title (PageTitle/make config title)
        page-id (PageId. page-title -1)
        cp (.postprocess engine page-id text nil)
        wrap-col 80
        p (WikiTextConverter. config wrap-col)
        wiki-text (.go p (.getPage cp))
        links (into #{} (.getLinks p))]
    {:links links}))

(defn revisions
  [title]
  (let [wiki (Wiki. "en.wikipedia.org")
        hist (reverse
              (.getPageHistory wiki title))]
    (map #(let [text (.getText %)
                parsed (parse title text)]
            (hash-map :user (.getUser %)
                      :timestamp (.getTimeInMillis
                                  (.getTimestamp %))
                      :links (:links parsed)))
         hist)))

(defn title->symbol
  [title]
  (sym/build
   [(sym/str->symbol title) "enwiki"]))

(defn revision->beliefs
  [title rev]
  (map
   #(vector "related/1" (title->symbol title) (title->symbol %))
   (:links rev)))

(defn with-beliefs
  [title revs]
  (map #(assoc % :beliefs (revision->beliefs title %))
       revs))

(defn rev-with-belief-changes
  [prev-rev rev]
  (let [prev-beliefs (into #{} (:beliefs prev-rev))
        beliefs (into #{} (:beliefs rev))
        new-beliefs (filter #(not (contains? prev-beliefs %)) beliefs)
        lost-beliefs (filter #(not (contains? beliefs %)) prev-beliefs)]
    (assoc rev
      :new-beliefs (into #{} new-beliefs)
      :lost-beliefs (into #{} lost-beliefs))))

(defn with-belief-changes
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
             (conj result (rev-with-belief-changes prev-rev rev))))))

(defn user->symbol
  [user]
  (if (re-matches
       #"^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
       user)
    "anon/enwiki_usr_spec"
    (sym/build
     [(sym/str->symbol user) "enwiki_usr"])))

(defn read!
  [hg title]
  (println (map #(str "+" (count (:new-beliefs %))
                      " -" (count (:lost-beliefs %))
                      " [" (user->symbol (:user %)) "]")
                (with-belief-changes
                  (with-beliefs title
                    (revisions title))))))

