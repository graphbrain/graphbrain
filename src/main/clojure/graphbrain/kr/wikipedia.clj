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
        hist (.getPageHistory wiki title)]
    (map #(let [text (.getText %)
                parsed (parse title text)]
            (hash-map :user (.getUser %)
                      :timestamp (.getTimeInMillis
                                  (.getTimestamp %))
                      :links (:links parsed)))
         hist)))

(defn read!
  [hg title]
  (println (map #(:user %) (revisions title))))

(defn process!
  [hg page]
  (let [wiki-text (slurp
                   (str "https://en.wikipedia.org/wiki/" page "?action=raw"))
        config (DefaultConfigEnWp/generate)
        engine (WtEngineImpl. config)
        page-title (PageTitle/make config page)
        page-id (PageId. page-title -1)
        cp (.postprocess engine page-id wiki-text nil)
        wrap-col 80
        p (WikiTextConverter. config wrap-col)
        text (.go p (.getPage cp))
        links (.getLinks p)]
    (prn links)
    (prn "done.")))
