(ns graphbrain.hg.mysql
  (:use graphbrain.utils)
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.math.combinatorics :as combo]
            [graphbrain.hg.edgestr :as es])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(def MYSQL_ENGINE "InnoDB")

(defn- safe-exec!
  [dbs sql]
  (try (jdbc/db-do-commands dbs sql)
    (catch Exception e (exception->str e))))

(defn- create-tables!
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
  [name]
  (let [conn {:classname "com.mysql.jdbc.Driver"
              :subprotocol "mysql"
              :subname (str "//127.0.0.1:3306/" name)
              :user "gb"
              :password "gb"}]
    (create-tables! conn)))

(defn- pool
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

(def pooled-db (delay (pool (db-spec "gbnode"))))

(defn- db-connection [] @pooled-db)

(defn- do-with-edge-permutations!
  [edge f]
  (let [nperms (combo/count-permutations edge)
        perms (map #(str (clojure.string/join " "
                                              (map es/edge->str (combo/nth-permutation edge %)))
                         " " %)
                   (range nperms))]
    (doseq [perm-str perms] (f perm-str))))

(defn- write-edge-permutations!
  [conn edge]
  (do-with-edge-permutations! edge #(jdbc/execute!
                                     (conn)
                                     ["INSERT INTO perms (id) VALUES (?)"
                                      (es/edge->str %)])))

(defn- remove-edge-permutations!
  [conn edge]
  (do-with-edge-permutations! edge #(jdbc/execute!
                                     (conn)
                                     ["DELETE FROM perms WHERE id=?"
                                      (es/edge->str %)])))

(defn- exists-str?
  [conn edge-str]
  (let [rs (jdbc/query (conn) [(str "SELECT id FROM edges WHERE id=?")
                              edge-str])]
    (not (empty? rs))))

(defn exists?
  [hg edge]
  (exists-str? (:conn hg) (es/edge->str edge)))

(defn- add-str!
  [conn edge-str]
  (jdbc/execute! (conn) ["INSERT INTO edges (id) VALUES (?)" edge-str]))

(defn add!
  [hg edge]
  (if (not (exists? hg edge))
    (let [conn (:conn hg)]
      (add-str! conn edge (es/edge->str edge))
      (write-edge-permutations! conn edge)))
  edge)

(defn- remove-str!
  [conn edge-str]
  (jdbc/delete! (conn) :edges ["id=?" edge-str]))

(defn remove!
  [hg edge]
  (let [conn (:conn hg)]
    (remove-str! conn (es/edge->str edge))
    (remove-edge-permutations! conn edge)))

(defn- unpermutate
  [tokens nper]
  (let [n (count tokens)
        indices (apply vector (combo/nth-permutation (range n) nper))
        inv-indices (reduce #(assoc %1 (nth indices %2) %2) {} (range n))]
    (apply vector (map #(nth tokens (inv-indices %)) (range n)))))

(defn- results->edges
  [rs]
  (loop [results rs
         edges #{}]
    (if (empty? results) edges
      (let [res (first results)
            tokens (es/split-edge-inner-str res)
            nper (Integer. (last tokens))
            tokens (drop-last tokens)
            tokens (unpermutate tokens nper)
            edge (es/str->edge
                  (str "(" (clojure.string/join " " tokens) ")"))
            edges (conj edges edge)]
        (recur (rest results) edges)))))

(defn- str+1
  [str]
  (clojure.string/join
   (concat (drop-last str) (list (char (inc (int (last str))))))))

(defn edge-matches-pattern?
  [edge pattern]
  (every? identity
          (map #(or (= %2 "*") (= %1 %2)) (map #(es/edge->str) edge) pattern)))

(defn pattern->edges
  [hg pattern]
  (let [ids (filter #(not= % "*") pattern)
        start-str (str (clojure.string/join " " ids) " ")
        end-str (str+1 start-str)
        rs (jdbc/query (:conn hg) ["SELECT id FROM perms WHERE id>=? AND id<?"
                              start-str end-str])]
    (filter #(edge-matches-pattern? % pattern) (results->edges rs))))

(defn- str->edges
  [conn center-id]
  (let [start-str (str center-id " ")
        end-str (str+1 start-str)
        rs (jdbc/query (conn) ["SELECT id FROM perms WHERE id>=? AND id<?"
                              start-str end-str])]
    (results->edges rs)))

(defn neighbors
  [hg center]
  (let [conn (:conn hg)]
    (if (coll? center)
      (str->edges conn (es/edge->str center))
      (str->edges conn center))))

(defn mysql-hg
  [dbname]
  {:conn (db-connection)
   :exists? exists?
   :add! add!
   :remove! remove!
   :pattern->edges pattern->edges
   :neighbors neighbors})
