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
  ;; Edges table
  (sql/safe-exec! conn "CREATE TABLE edges (id TEXT PRIMARY KEY)")
  ;; Edge permutations table
  (sql/safe-exec! conn "CREATE TABLE perms (id TEXT PRIMARY KEY)")
  conn)

(defn- db-spec
  "Generates SQLite connection specifications map."
  [name]
  (let [conn {:classname "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname (str name ".db")}]
    (create-tables! conn)))

(defn- db-connection
  "Create a database connection."
  [name]
  (db-spec name))

(deftype SQLiteOps [conn]
  ops/Ops
  (exists? [hg edge] (sql/exists? conn edge))
  (add! [hg edges] (sql/add! conn edges))
  (remove! [hg edges] (sql/remove! conn edges))
  (pattern->edges [hg pattern] (sql/pattern->edges conn pattern))
  (star [hg center] (sql/star conn center))
  (symbols-with-root [hg root] (sql/symbols-with-root conn root))
  (destroy! [hg] (sql/destroy! conn))
  (exec! [hg ops] (sql/exec! conn ops)))

(defn connection
  "Obtain a SQLite hypergraph connection."
  [dbname]
  (SQLiteOps. (db-connection dbname)))
