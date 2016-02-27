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

(ns graphbrain.web.handlers.input
  (:require [graphbrain.hg.symbol :as symb]
            [graphbrain.hg.constants :as const]
            [graphbrain.hg.beliefs :as beliefs]
            [graphbrain.kr.pagereader :as pr]
            [graphbrain.eco.eco :as eco]
            [graphbrain.eco.parsers.chat :as chat]
            [graphbrain.disambig.edgeguesser :as edg]
            [graphbrain.web.handlers.search :as search]))

(defn- goto-id
  [root edge]
  (if (coll? edge)
    (if (= (first edge) "edges/1")
      (goto-id root (first edge))
      (second edge))
    root))

(defn- input-reply-fact
  [root edge]
  (pr-str {:type :fact
           :newedges (list edge)
           :gotoid (goto-id root edge)}))

(defn- input-reply-url
  [root]
  (pr-str {:type :url
           :gotoid root}))

(defn- input-reply-search
  [count results mode]
  (pr-str {:type :search
           :mode mode
           :count count
           :results results}))

(defn- input-reply-definition
  [root rel]
  (pr-str {:type :def
           :root-id root
           :rel rel}))

(defn- input-reply-error
  [msg]
  (pr-str {:type :error
           :msg msg}))

(defn- sentence-type
  [sentence]
  (cond
   (and (not (some #(= \space %) sentence))
        (or (.startsWith sentence "http://")
            (.startsWith sentence "https://"))) :url
            (.startsWith sentence "x ") :intersect
            :else :fact))

(defn- definer?
  [edge]
  (= (first edge) "is/eco"))

(defn- definer->rel
  [edge]
  (case (first edge)
    "is/eco" const/type-of))

(defn- process-fact
  [hg request root sentence]
  (let [env {:root root
             :user "user/1"}
        res (eco/parse-str chat/chat sentence env)]
    (if (coll? res)
      (if (and (symb/root? root) (definer? res))
        (input-reply-definition root
                                (definer->rel res))
        (let [edge (edg/guess hg res sentence)]
          (beliefs/add! hg "user/1" edge)
          (input-reply-fact root edge)))
      (input-reply-error "Sorry, I don't understand."))))

(defn process-search
  [hg request root q mode]
  (let [q (if (= mode :intersect)
            (clojure.string/trim (subs q 1))
            q)
        results (search/query hg q)]
    (if (empty? results)
      (process-fact hg request root q)
      (search/reply results mode))))

(defn process-url
  [hg request root url]
  (pr/extract-knowledge! hg url)
  (input-reply-url url))

(defn handle
  [request hg]
  (let [sentence ((request :form-params) "sentence")
        root ((request :form-params) "root")]
    (case (sentence-type sentence)
      :fact (process-search hg request root sentence :search)
      :url (process-url hg request root sentence)
      :intersect (process-search hg request root sentence :intersect))))
