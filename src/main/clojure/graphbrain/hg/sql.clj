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
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.math.combinatorics :as combo]
            [graphbrain.hg.ops :as ops]
            [graphbrain.hg.edgestr :as es]))

(defn safe-exec!
  "Tries to execute an SQL command and ignores exceptions."
  [dbs sql]
  (try (jdbc/db-do-commands dbs sql)
    (catch Exception e)))

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
  "Check if the given vertex, represented as a string, exists."
  [conn vert-str]
  (let [rs (jdbc/query conn ["SELECT id FROM vertices WHERE id=?" vert-str])]
    (not (empty? rs))))

(defn- remove-str!
  "Removes the given vertex, represented as a string."
  [conn vert-str]
  (jdbc/delete! conn :vertices ["id=?" vert-str]))

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
  "Query database for all the edge permutations that contain a given entity,
   represented as a string."
  [conn center-id]
  (let [start-str (str center-id " ")
        end-str (str+1 start-str)
        rs (jdbc/query conn ["SELECT id FROM perms WHERE id>=? AND id<?"
                              start-str end-str])]
    (results->edges rs)))

(defn exists?
  "Checks if the given vertex exists in the hypergraph."
  [conn vertex]
  (exists-str? conn (es/edge->str vertex)))

(defn- inc-degree!
  "Increments the degree of a vertex."
  [conn vert-str]
  (jdbc/execute! conn ["UPDATE vertices SET degree=degree+1 WHERE id=?" vert-str]))

(defn- dec-degree!
  "Decrements the degree of a vertex."
  [conn vert-str]
  (jdbc/execute! conn ["UPDATE vertices SET degree=degree-1 WHERE id=?" vert-str]))

(defn- add-raw!
  "Auxiliary function for add! to call from inside a transaction."
  [conn edge add-str!]
  (if (not (exists? conn edge))
    (do
      (add-str! conn (es/edge->str edge))
      (doseq [vert edge]
        (add-str! conn (es/edge->str vert))
        (inc-degree! conn (es/edge->str vert)))
      (write-edge-permutations! conn edge)))
  edge)

(defn add!
  "Adds one or multiple edges to the hypergraph if it does not exist yet.
   Adding multiple edges at the same time might be faster."
  [conn edges add-str!]
  (jdbc/with-db-transaction [trans-conn conn]
    (if (coll? (first edges))
      (doseq [edge edges]
        (add-raw! trans-conn edge add-str!))
      (add-raw! trans-conn edges add-str!)))
  edges)

(defn- remove-raw!
  "Auxiliary function for remove! to call from inside a transaction."
  [conn edge]
  (do
    (remove-str! conn (es/edge->str edge))
    (doseq [vert edge]
      (dec-degree! conn (es/edge->str vert)))
    (remove-edge-permutations! conn edge)))

(defn remove!
  "Removes one or multiple edges from the hypergraph.
   Removing multiple edges at the same time might be faster."
  [conn edges]
  (jdbc/with-db-transaction [trans-conn conn]
    (if (coll? (first edges))
      (doseq [edge edges]
        (remove-raw! trans-conn edge))
      (remove-raw! trans-conn edges))))

(defn pattern->edges
  "Return all the edges that match a pattern.
   A pattern is a collection of entity ids and wildcards (nil)."
  [conn pattern]
  (let [nodes (filter #(not (nil? %)) pattern)
        start-str (es/nodes->str nodes)
        end-str (str+1 start-str)
        rs (jdbc/query conn ["SELECT id FROM perms WHERE id>=? AND id<?"
                             start-str end-str])
        edges (results->edges rs)]
    (filter #(edge-matches-pattern? % pattern) edges)))

(defn star
  "Return all the edges that contain a given entity.
   Entity can be atomic or an edge."
  [conn center]
  (if (coll? center)
    (str->perms conn (es/edge->str center))
    (str->perms conn center)))

(defn symbols-with-root
  "Find all symbols with the given root."
  [conn root]
  (let [start-str (str root "/")
        end-str (str+1 start-str)
        results (jdbc/query conn ["SELECT id FROM perms WHERE id>=? AND id<?"
                                  start-str end-str])
        symbs (map #(first (es/split-edge-str (:id %))) results)]
    (set symbs)))

(defn destroy!
  "Erase the hypergraph."
  [conn]
  (jdbc/execute! conn ["DELETE FROM vertices"])
  (jdbc/execute! conn ["DELETE FROM perms"]))

(defn degree
  "Returns the degree of a vertex."
  [conn vertex]
  (let [vert-str (es/edge->str vertex)
        rs (jdbc/query conn ["SELECT id FROM vertices WHERE id=?" vert-str])]
    (:degree (first rs))))
