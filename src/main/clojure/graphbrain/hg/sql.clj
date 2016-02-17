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

(ns graphbrain.hg.sql
  "Generic functions for SQL database storage."
  (:use graphbrain.utils.utils)
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.math.combinatorics :as combo]
            [graphbrain.hg.ops :as ops]
            [graphbrain.hg.edgestr :as es]))

(defn safe-exec!
  "Tries to execute an SQL command and ignores exceptions."
  [dbs sql]
  (try (jdbc/db-do-commands dbs sql)
    (catch Exception e (exception->str e))))

(defn- do-with-edge-permutations!
  "Applies the function f to all permutations of the given edge."
  [edge f]
  (let [nperms (combo/count-permutations edge)
        perms (map
               #(str
                 (clojure.string/join " "
                                      (map es/edge->str
                                           (combo/nth-permutation edge %)))
                 " " %)
               (range nperms))]
    (doseq [perm-str perms] (f perm-str))))

(defn- write-edge-permutations!
  "Writes all permutations of the given edge."
  [conn edge]
  (do-with-edge-permutations! edge #(jdbc/execute!
                                     conn
                                     ["INSERT INTO perms (id) VALUES (?)"
                                      (es/edge->str %)])))

(defn- remove-edge-permutations!
  "Removes all permutations of the given edge."
  [conn edge]
  (do-with-edge-permutations! edge #(jdbc/execute!
                                     conn
                                     ["DELETE FROM perms WHERE id=?"
                                      (es/edge->str %)])))

(defn- exists-str?
  "Check if the given edge, represented as a string, exists."
  [conn edge-str]
  (let [rs (jdbc/query conn ["SELECT id FROM edges WHERE id=?" edge-str])]
    (not (empty? rs))))

(defn- add-str!
  "Adds the given edge, represented as a string."
  [conn edge-str]
  (jdbc/execute! conn ["INSERT INTO edges (id) VALUES (?)" edge-str]))

(defn- remove-str!
  "Removes the given edge, represented as a string."
  [conn edge-str]
  (jdbc/delete! conn :edges ["id=?" edge-str]))

(defn- unpermutate
  "Reorder the tokens vector to revert a permutation, specified by nper."
  [tokens nper]
  (let [n (count tokens)
        indices (apply vector (combo/nth-permutation (range n) nper))
        inv-indices (reduce #(assoc %1 (nth indices %2) %2) {} (range n))]
    (apply vector (map #(nth tokens (inv-indices %)) (range n)))))

(defn- results->edges
  "Transforms a results object from a database query into a set of edges."
  [rs]
  (loop [results rs
         edges #{}]
    (if (empty? results) edges
        (let [res (:id (first results))
              tokens (es/split-edge-str res)
              nper (Integer. (last tokens))
              tokens (drop-last tokens)
              tokens (unpermutate tokens nper)
              edge (es/str->edge
                    (str "(" (clojure.string/join " " tokens) ")"))
              edges (conj edges edge)]
          (recur (rest results) edges)))))

(defn- str+1
  "Increment a string by one, regaring lexicographical ordering."
  [str]
  (clojure.string/join
   (concat (drop-last str)
           (list (char (inc (int (last str))))))))

(defn edge-matches-pattern?
  "Check if an edge matches a pattern."
  [edge pattern]
  (every? identity
          (map #(or (nil? %2) (= %1 %2)) edge pattern)))

(defn- str->perms
  "Query database for all the edge permutations that contain a given entity, represented as a string."
  [conn center-id]
  (let [start-str (str center-id " ")
        end-str (str+1 start-str)
        rs (jdbc/query conn ["SELECT id FROM perms WHERE id>=? AND id<?"
                              start-str end-str])]
    (results->edges rs)))

(defn exists?
  [conn edge]
  (exists-str? conn (es/edge->str edge)))

(defn- add-raw!
  [conn edge]
  (if (not (exists? conn edge))
    (do
      (add-str! conn (es/edge->str edge))
      (write-edge-permutations! conn edge)))
  edge)

(defn add!
  [conn edges]
  (jdbc/with-db-transaction [trans-conn conn]
    (if (coll? (first edges))
      (doseq [edge edges]
        (add-raw! trans-conn edge))
      (add-raw! trans-conn edges)))
  edges)

(defn- remove-raw!
  [conn edge]
  (do
    (remove-str! conn (es/edge->str edge))
    (remove-edge-permutations! conn edge)))

(defn remove!
  [conn edges]
  (jdbc/with-db-transaction [trans-conn conn]
    (if (coll? (first edges))
      (doseq [edge edges]
        (remove-raw! trans-conn edge))
      (remove-raw! trans-conn edges))))

(defn pattern->edges
  [conn pattern]
  (let [nodes (filter #(not (nil? %)) pattern)
        start-str (es/nodes->str nodes)
        end-str (str+1 start-str)
        rs (jdbc/query conn ["SELECT id FROM perms WHERE id>=? AND id<?"
                             start-str end-str])
        edges (results->edges rs)]
    (filter #(edge-matches-pattern? % pattern) edges)))

(defn star
  [conn center]
  (if (coll? center)
    (str->perms conn (es/edge->str center))
    (str->perms conn center)))

(defn destroy!
  [conn]
  (jdbc/execute! conn ["DELETE FROM edges"])
  (jdbc/execute! conn ["DELETE FROM perms"]))
