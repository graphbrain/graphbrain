(ns graphbrain.db.mysql
  (:use graphbrain.utils)
  (:require [clojure.java.jdbc :as jdbc]
            [graphbrain.db.edge :as edge]
            [graphbrain.db.edgeparser :as edgeparser])
  (:import (com.graphbrain.utils Permutations)))

(defonce MYSQL_ENGINE "InnoDB")

(defn safe-exec!
  [dbs sql]
  (try (jdbc/db-do-commands dbs sql)
       (catch Exception e (exception->str e))))

(defn create-tables!
  [dbs]
  ;; Edges table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS edges ("
                      "id VARCHAR(10000),"
                      "degree INT DEFAULT 0,"
                      "ts BIGINT DEFAULT -1,"
                      "INDEX id_index (id(255))"
                      ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; EdgeTypes table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS edgetypes ("
                      "id VARCHAR(10000),"
                      "degree INT DEFAULT 0,"
                      "ts BIGINT DEFAULT -1,"
                      "label VARCHAR(255),"
                      "INDEX id_index (id(255))"
                      ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; Entities table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS entities ("
                      "id VARCHAR(10000),"
                      "degree INT DEFAULT 0,"
                      "ts BIGINT DEFAULT -1,"
                      "INDEX id_index (id(255))"
                      ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; URLs table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS urls ("
                      "id VARCHAR(10000),"
                      "degree INT DEFAULT 0,"
                      "ts BIGINT DEFAULT -1,"
                      "title VARCHAR(500),"
                      "icon VARCHAR(500),"
                      "INDEX id_index (id(255))"
                      ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; Users table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS users ("
                      "id VARCHAR(10000),"
                      "degree INT DEFAULT 0,"
                      "ts BIGINT DEFAULT -1,"
                      "username VARCHAR(255),"
                      "name VARCHAR(255),"
                      "email VARCHAR(255),"
                      "pwdhash VARCHAR(255),"
                      "role VARCHAR(255),"
                      "session VARCHAR(255),"
                      "session_ts BIGINT DEFAULT -1,"
                      "last_seen BIGINT DEFAULT -1,"
                      "INDEX id_index (id(255)),"
                      "INDEX email_index (email)"
                      ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; Progs table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS progs ("
                      "id VARCHAR(10000),"
                      "degree INT DEFAULT 0,"
                      "ts BIGINT DEFAULT -1,"
                      "prog TEXT,"
                      "INDEX id_index (id(255))"
                      ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; Texts table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS texts ("
                      "id VARCHAR(10000),"
                      "degree INT DEFAULT 0,"
                      "ts BIGINT DEFAULT -1,"
                      "text TEXT,"
                      "INDEX id_index (id(255))"
                      ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; Edge permutations table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS edgeperms ("
                      "id VARCHAR(10000),"
                      "INDEX id_index (id(255))"
                      ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; Global-User table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS globaluser ("
                      "global_id VARCHAR(10000),"
                      "user_id VARCHAR(10000),"
                      "INDEX global_id_index (global_id(255))"
                      ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))
  dbs)

(defn db-spec
  [name]
  (let [dbs {:classname "com.mysql.jdbc.Driver"
             :subprotocol "mysql"
             :subname (str "//127.0.0.1:3306/" name)
             :user "gb"
             :password "gb"}]
    (create-tables! dbs)))

(defn- vtype->tname
  [vtype]
  (case vtype
    :edge "edges"
    :edge-type "edgetypes"
    :entity "entities"
    :url "urls"
    :user "users"
    :prog "progs"
    :text "texts"))

(defn- do-with-edge-permutations!
  [edge f]
  (let [ids (:ids edge)
        nperms (Permutations/permutations (count ids))
        perms (map #(str (Permutations/strArrayPermutationToStr ids %) " " %) (range nperms))]
    (doseq [perm-id perms] (f perm-id))))

(defn- write-edge-permutations!
  [dbs edge]
  (do-with-edge-permutations! edge #(jdbc/execute!
                                     dbs
                                     ["INSERT INTO edgeperms (id) VALUES (?)" %])))

(defn- remove-edge-permutations!
  [dbs edge]
  (do-with-edge-permutations! edge #(jdbc/execute!
                                     dbs
                                     ["DELETE FROM edgeperms WHERE id=?" %])))

(defn- get-vertex
  [dbs id table]
  (first (jdbc/query dbs [(str "SELECT * FROM " table " WHERE id=?") id])))

(defn- exists-vertex?
  [dbs id table]
  (let [rs (jdbc/query dbs ["SELECT EXISTS(SELECT 1 FROM ? WHERE id=?)" table id])]
    (= 1 (:1 (first rs)))))

(defn- put-vertex!
  [dbs vertex table]
  (jdbc/insert! dbs (keyword table) (dissoc vertex :type)))

(defn- update-vertex!
  [dbs vertex table]
  (jdbc/update! dbs (keyword table) (dissoc vertex :type) ["id = ?" (:id vertex)])
  vertex)

(defn- remove-vertex!
  [dbs id table]
  (jdbc/delete! dbs (keyword table) ["id = ?" id]))

(defn getv
  [dbs id vtype]
  (assoc (get-vertex dbs id (vtype->tname vtype)) :type vtype))

(defn exists?
  [dbs id vtype]
  (exists-vertex? dbs id (vtype->tname vtype)))

(defn putv!
  [dbs vertex]
  (let [vtype (:type vertex)]
    (put-vertex! dbs vertex (vtype->tname vtype))
    (if (= vtype :edge) (write-edge-permutations! dbs vertex))
    vertex))
        
(defn update!
  [dbs vertex]
  (update-vertex! dbs vertex (vtype->tname (:type vertex))))

(defn remove!
  [dbs vertex]
  (let [vtype (:type vertex)]
    (remove-vertex! dbs (:id vertex) (vtype->tname vtype))
    (if (= vtype :edge) (remove-edge-permutations! dbs vertex))))

(defn email->username
  [dbs email]
  (:username (jdbc/query dbs ["SELECT username FROM users WHERE email=?" email])))

(defn all-users
  [dbs]
  (map #(assoc % :type :user)
       (jdbc/query dbs "SELECT * FROM users")))

(defn- results->edges
  [rs]
  (loop [results rs
           edges #{}]
      (if (empty? results) edges
          (let [res (first results)
                pid (:id res)
                tokens (edgeparser/split-edge pid)
                perm (Integer. (last tokens))
                tokens (drop-last tokens)
                tokens (Permutations/strArrayUnpermutate (into-array tokens) perm)
                edge (edge/ids->edge tokens)
                edges (conj edges edge)]
            (recur (rest results) edges)))))

(defn- str+1
  [str]
  (clojure.string/join
   (concat (drop-last str) (list (char (inc (int (last str))))))))

(defn pattern->edges
  [dbs pattern]
  (let [start-str (clojure.string/join
                   (filter #(not= % "*") (:ids pattern)) " ") 
        end-str (str+1 start-str)
        rs (jdbc/query dbs ["SELECT id FROM edgeperms WHERE id>=? AND id<?"
                            start-str end-str])]
    (filter #(edge/matches? % pattern) (results->edges rs))))

(defn id->edges
  [dbs center-id]
  (let [start-str (str center-id " ")
        end-str (str+1 start-str)
        rs (jdbc/query dbs ["SELECT id FROM edgeperms WHERE id>=? AND id<?"
                            start-str
                            end-str])]
    (results->edges rs)))

(defn add-link-to-global!
  [dbs global-id user-id]
  (jdbc/execute! dbs ["INSERT INTO globaluser (global_id, user_id) VALUES (?, ?)"
                      global-id
                      user-id]))

(defn remove-link-to-global!
  [dbs global-id user-id]
  (jdbc/execute! dbs ["DELETE FROM globaluser WHERE global_id=? AND user_id=?"
                      global-id
                      user-id]))

(defn alts
  [dbs global-id]
  (map :user_id (jdbc/query dbs ["SELECT user_id FROM globaluser WHERE global_id=?"
                                 global-id])))
