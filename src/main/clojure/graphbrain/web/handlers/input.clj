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
  (:require [graphbrain.web.handlers.search :as search]
            [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.symbol :as sym]
            [graphbrain.disambig.edgeguesser :as edg]
            [graphbrain.kr.pagereader :as pr]
            [graphbrain.eco.eco :as eco]
            [graphbrain.eco.parsers.chat :as chat]))

(defn- goto-id
  [root-id vertex]
  #_(if (maps/edge? vertex)
    (if (= (maps/edge-type vertex) "r/*edges")
      (goto-id root-id
               (maps/id->edge
                (first
                 (maps/participant-ids vertex))))
      (id/eid->id
       (second
        (maps/ids vertex))))
    root-id)
  )

(defn- input-reply-fact
  [root-id vertex]
  (pr-str {:type :fact
           :newedges (list (:id vertex))
           :gotoid (goto-id root-id vertex)}))

(defn- input-reply-url
  [root-id]
  (pr-str {:type :url
           :gotoid root-id}))

(defn- input-reply-search
  [count results mode]
  (pr-str {:type :search
           :mode mode
           :count count
           :results results}))

(defn- input-reply-definition
  [root-id rel param]
  (pr-str {:type :def
           :root-id root-id
           :rel rel
           :param param}))

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

(defn- is-definer?
  [edge]
  #_(= (id/edge-rel edge) "r/is"))

(defn- definer->rel
  [edge]
  #_(case (id/edge-rel edge)
    "r/is" "r/+t"))

(defn- definer->param
  [edge]
  #_(case (id/edge-rel edge)
    "r/is" (-> edge
               id/id->ids
               (nth 2))))

(defn- process-fact
  [hg request root sentence]
  #_(let
      [root-id (:id root)
       env {:root root-id
            :user (:id user)}
       res (eco/parse-str chat/chat sentence env)]
    (if (id/edge? res)
      (if (perms/can-edit? common/gbdb (:id user) ctxt)
        (if (and (id/undefined-eid? (:eid root))
                 (is-definer? res))
          (input-reply-definition root-id
                                  (definer->rel res)
                                  (definer->param res))
          (let [edge-id (edg/guess common/gbdb res sentence (:id user) ctxts)
                edge (maps/id->vertex edge-id)
                edge (assoc edge :score 1)]
            (k/addfact! common/gbdb edge ctxt (:id user))
            (common/log request (str "fact added: " edge
                                     "; input: " sentence
                                     "; ctxt: " ctxt
                                     (if root-id (str "; root: " root-id))))
            (input-reply-fact root-id edge)))
        (do
          (common/log request (str "INPUT FAILED (no permissions). "
                                   "input: " sentence
                                   "; ctxt: " ctxt
                                   (if root-id (str "; root: " root-id))))
          (input-reply-error
           "Sorry, you don't have permissions to edit this GraphBrain.")))
      (do
        (common/log request (str "INPUT FAILED (don't understand). "
                                 "input: " sentence
                                 "; ctxt: " ctxt
                                 (if root-id (str "; root: " root-id))))
        (input-reply-error "Sorry, I don't understand.")))))

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
  [hg request root sentence]
  #_(let [url-id (url/url->id sentence)
        root-id (:id root)]
    (pr/extract-knowledge! common/gbdb sentence ctxt ctxts (:id user))
    (common/log request (str "extract knowledge from url "
                             "input: " sentence
                             "; ctxt: " ctxt
                             (if root-id (str "; root: " root-id))))
    (input-reply-url url-id)))

(defn handle
  [request hg]
  (let [sentence ((request :form-params) "sentence")
        root ((request :form-params) "root")]
    (case (sentence-type sentence)
      :fact (process-search hg request root sentence :search)
      :url (process-url hg request root sentence)
      :intersect (process-search hg request root sentence :intersect))))
