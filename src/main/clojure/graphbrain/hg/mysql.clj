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

(ns graphbrain.hg.mysql
  "Implements MySQL hypergraph storage."
  (:use graphbrain.utils.utils)
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.math.combinatorics :as combo]
            [graphbrain.hg.ops :as ops]
            [graphbrain.hg.edgestr :as es])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(def MYSQL_ENGINE "InnoDB")

(defn- safe-exec!
  "Tries to execute an SQL command and ignores exceptions."
  [dbs sql]
  (try (jdbc/db-do-commands dbs sql)
    (catch Exception e (exception->str e))))

(defn- create-tables!
  "Created the tables necessary to store the hypergraph."
  [conn]
  ;; Edges table
  (safe-exec! conn (str "CREATE TABLE IF NOT EXISTS edges ("
                        "id VARCHAR(10000),"
                        "INDEX id_index (id(255))"
                        ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  (safe-exec! conn (str "CREATE INDEX id_ts_edges_index ON edges (id, ts);"))
  
  ;; Edge permutations table
  (safe-exec! conn (str "CREATE TABLE IF NOT EXISTS perms ("
                        "id VARCHAR(10000),"
                        "INDEX id_index (id(255))"
                        ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  conn)

(defn- db-spec
  "Generates MySQL connection specifications map."
  [name]
  (let [conn {:classname "com.mysql.jdbc.Driver"
              :subprotocol "mysql"
              :subname (str "//127.0.0.1:3306/" name)
              :user "gb"
              :password "gb"}]
    (create-tables! conn)))

(defn- pool
  "Creates a connection poll for the given specifications."
  [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))
               (.setPassword (:password spec))
               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))

(defn- db-connection
  "Create a database connection."
  [name]
  @(delay (pool (db-spec name))))

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

(defn- exists-mysql?
  "Internal implementation of exists? function."
  [conn edge]
  (exists-str? conn (es/edge->str edge)))

(deftype MySQLOps [conn]
  ops/Ops

  (exists?
    [hg edge]
    (exists-mysql? conn edge))

  (add!
    [hg edge]
    (if (not (exists-mysql? conn edge))
      (do
        (add-str! conn (es/edge->str edge))
        (write-edge-permutations! conn edge)))
    edge)

  (remove!
    [hg edge]
    (do
      (remove-str! conn (es/edge->str edge))
      (remove-edge-permutations! conn edge)))

  (pattern->edges
    [hg pattern]
    (let [nodes (filter #(not (nil? %)) pattern)
          start-str (es/nodes->str nodes)
          end-str (str+1 start-str)
          rs (jdbc/query conn ["SELECT id FROM perms WHERE id>=? AND id<?"
                               start-str end-str])
          edges (results->edges rs)]
      (filter #(edge-matches-pattern? % pattern) edges)))

  (star
    [hg center]
    (if (coll? center)
      (str->perms conn (es/edge->str center))
      (str->perms conn center))))

(defn connection
  "Obtain a MySQL hypergraph connection."
  [dbname]
  (MySQLOps. (db-connection dbname)))
