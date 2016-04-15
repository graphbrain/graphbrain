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

(ns graphbrain.hg.sqlite
  "Implements SQLite hypergraph storage."
  (:require [clojure.java.jdbc :as jdbc]
            [graphbrain.hg.sql :as sql]
            [graphbrain.hg.ops :as ops]))

(defn- create-tables!
  "Created the tables necessary to store the hypergraph."
  [conn]
  ;; Vertices table
  (sql/safe-exec! conn (str "CREATE TABLE vertices ("
                            "id TEXT PRIMARY KEY,"
                            "degree INTEGER DEFAULT 0,"
                            "timestamp INTEGER DEFAULT -1"
                            ")"))
  
  ;; Edge permutations table
  (sql/safe-exec! conn "CREATE TABLE perms (id TEXT PRIMARY KEY)")
  conn)

(defn- db-spec
  "Generates SQLite connection specifications map."
  [name]
  (let [conn {:classname "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname name}]
    (create-tables! conn)))

(defn- db-connection
  "Create a database connection."
  [name]
  (db-spec name))

(declare create-with-conn)

(deftype SQLiteOps [conn]
  ops/Ops
  (exists? [hg edge] (sql/exists? conn edge))
  (add!* [hg edges timestamp] (sql/add!* conn edges timestamp))
  (remove! [hg edges] (sql/remove! conn edges))
  (pattern->edges [hg pattern] (sql/pattern->edges conn pattern))
  (star [hg center] (sql/star conn center))
  (symbols-with-root [hg root] (sql/symbols-with-root conn root))
  (destroy! [hg] (sql/destroy! conn))
  (degree [hg vertex] (sql/degree conn vertex))
  (timestamp [hg vertex] (sql/timestamp conn vertex))
  (batch-exec! [hg funs] (sql/batch-exec! conn funs create-with-conn))
  (f-all [hg f] (sql/f-all conn f)))

(defn connection
  "Obtain a SQLite hypergraph connection."
  [dbname]
  (SQLiteOps. (db-connection dbname)))

(defn- create-with-conn
  ;; Cretes an intance of the hypergraph using the provided
  ;; sqlite-specific connection 'aconn'
  [aconn]
  (SQLiteOps. aconn))
