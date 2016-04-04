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
  (:require [clojure.java.jdbc :as jdbc]
            [graphbrain.hg.sql :as sql]
            [graphbrain.hg.ops :as ops])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(def MYSQL_ENGINE "InnoDB")

(defn- create-tables!
  "Created the tables necessary to store the hypergraph."
  [conn]
  ;; Vertices table
  (sql/safe-exec! conn (str "CREATE TABLE IF NOT EXISTS vertices ("
                            "id VARCHAR(10000),"
                            "degree INT DEFAULT 0"
                            ;;"UNIQUE KEY id_index (id(255))"
                            ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; Edge permutations table
  (sql/safe-exec! conn (str "CREATE TABLE IF NOT EXISTS perms ("
                            "id VARCHAR(10000)"
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

(deftype MySQLOps [conn]
  ops/Ops
  (exists? [hg edge] (sql/exists? conn edge))
  (add! [hg edges] (sql/add! conn edges))
  (remove! [hg edges] (sql/remove! conn edges))
  (pattern->edges [hg pattern] (sql/pattern->edges conn pattern))
  (star [hg center] (sql/star conn center))
  (symbols-with-root [hg root] (sql/symbols-with-root conn root))
  (destroy! [hg] (sql/destroy! conn))
  (degree [hg vertex] (sql/degree conn vertex))
  (create [hg aconn] (MySQLOps. aconn))
  (batch-exec! [hg funs] (sql/batch-exec! hg conn funs)))

(defn connection
  "Obtain a MySQL hypergraph connection."
  [dbname]
  (MySQLOps. (db-connection dbname)))
