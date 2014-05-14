(ns graphbrain.db.mysql
  (:use graphbrain.utils)
  (:require [clojure.java.jdbc :as jdbc]
            [graphbrain.db.vertex :as vertex]
            [graphbrain.db.edge :as edge]
            [graphbrain.db.edgeparser :as edgeparser]
            [graphbrain.db.id :as id])
  (:import (com.graphbrain.utils Permutations)))

(def global-owner "g")

(def MYSQL_ENGINE "InnoDB")

(defn safe-exec!
  [dbs sql]
  (try (jdbc/db-do-commands dbs sql)
       (catch Exception e (exception->str e))))

(defn create-tables!
  [dbs]
  ;; Edges table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS edges ("
                       "id VARCHAR(10000),"
                       "score FLOAT DEFAULT 1,"
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
                       "relid VARCHAR(10000),"
                       "degree INT DEFAULT 0,"
                       "ts BIGINT DEFAULT -1,"
                       "INDEX index_id_degree (id(255), degree),"
                       "INDEX index_id_ts (id(255), ts)"
                       ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; URLs table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS urls ("
                       "id VARCHAR(10000),"
                       "degree INT DEFAULT 0,"
                       "ts BIGINT DEFAULT -1,"
                       "title VARCHAR(500),"
                       "icon VARCHAR(500),"
                       "INDEX index_id_degree (id(255), degree),"
                       "INDEX index_id_ts (id(255), ts)"
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
                       "sessionts BIGINT DEFAULT -1,"
                       "lastseen BIGINT DEFAULT -1,"
                       "INDEX id_index (id(255)),"
                       "INDEX email_index (email)"
                       ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; Contexts table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS contexts ("
                       "id VARCHAR(10000),"
                       "name VARCHAR(10000),"
                       "access CHAR(10),"
                       "size INT DEFAULT 0,"
                       "degree INT DEFAULT 0,"
                       "ts BIGINT DEFAULT -1,"
                       "INDEX id_index (id(255))"
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
                       "params VARCHAR(10000),"
                       "INDEX id_index (id(255))"
                       ") ENGINE=" MYSQL_ENGINE " DEFAULT CHARSET=utf8;"))

  ;; Global-Local table
  (safe-exec! dbs (str "CREATE TABLE IF NOT EXISTS globallocal ("
                       "gid VARCHAR(10000),"
                       "lid VARCHAR(10000),"
                       "INDEX global_id_index (gid(255), lid(255))"
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
    :context "contexts"
    :prog "progs"
    :text "texts"))

(defn- do-with-edge-permutations!
  [edge f]
  (let [edge-id (:id edge)
        ids (edge/ids (vertex/local->global edge))
        owner-id (id/owner edge-id)
        owner-id (if owner-id owner-id global-owner)
        nperms (Permutations/permutations (count ids))
        ids (into-array ids)
        perms (map #(str owner-id " " (Permutations/strArrayPermutationToStr ids %) " " %)
                   (range nperms))]
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
  (let [rs (jdbc/query dbs [(str "SELECT id FROM " table " WHERE id=?")
                            id])]
    (not (empty? rs))))

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
  (let [v (get-vertex dbs id (vtype->tname vtype))]
    (if v (assoc v :type vtype))))

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
  (:username (first
              (jdbc/query dbs ["SELECT username FROM users WHERE email=?" email]))))

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
              owner-id (first tokens)
              perm (Integer. (last tokens))
              tokens (rest (drop-last tokens))
              tokens (Permutations/strArrayUnpermutate (into-array tokens) perm)
              tokens (if (= owner-id global-owner)
                       tokens
                       (map #(id/global->local % owner-id) tokens))
              edge (edge/ids->edge tokens)
              edges (conj edges edge)]
          (recur (rest results) edges)))))

(defn- str+1
  [str]
  (clojure.string/join
   (concat (drop-last str) (list (char (inc (int (last str))))))))

(defn pattern->edges
  [dbs pattern]
  (let [ids (filter #(not= % "*") pattern)
        owner-id (id/owner (first ids))
        owner-id (if owner-id owner-id global-owner)
        ids (map id/local->global ids)
        start-str (str owner-id " " (clojure.string/join " " ids))
        end-str (str+1 start-str)
        rs (jdbc/query dbs ["SELECT id FROM edgeperms WHERE id>=? AND id<?"
                            start-str end-str])]
    (filter #(edge/matches? % pattern) (results->edges rs))))

(defn id->edges
  [dbs center-id]
  (let [owner-id (id/owner center-id)
        owner-id (if owner-id owner-id global-owner)
        gcenter-id (id/local->global center-id)
        start-str (str owner-id " " gcenter-id " ")
        end-str (str+1 start-str)
        rs (jdbc/query dbs ["SELECT id FROM edgeperms WHERE id>=? AND id<?"
                            start-str
                            end-str])]
    (results->edges rs)))

(defn add-link-to-global!
  [dbs gid lid]
  (jdbc/execute! dbs ["INSERT INTO globallocal (gid, lid) VALUES (?, ?)"
                      gid
                      lid]))

(defn remove-link-to-global!
  [dbs gid lid]
  (jdbc/execute! dbs ["DELETE FROM globallocal WHERE gid=? AND lid=?"
                      gid
                      lid]))

(defn alts
  [dbs gid]
  (map :lid (jdbc/query dbs ["SELECT lid FROM globallocal WHERE gid=?"
                                 gid])))
