(ns graphbrain.db.user
  (:require [graphbrain.db.id :as id])
  (:import (org.mindrot.jbcrypt BCrypt)
           (java.math BigInteger)
           (com.graphbrain.utils RandUtils)))

(defn new-user
  [username name email password role]
  {:id (id/username->id username)
   :type :user
   :username username
   :name name
   :email email
   :pwdhash (BCrypt/hashpw password (BCrypt/gensalt))
   :role role
   :session ""
   :sessionts -1
   :lastseen -1
   :degree 0
   :ts -1})


(defn check-session
  [user candidate]
  (= (:session user) candidate))

(defn new-session
  [user]
  (assoc user :session (.toString (BigInteger. 130 (RandUtils/secRand)) 32)))

(defn check-password
  [user candidate]
  (BCrypt/checkpw candidate (:pwdhash user)))

(defn check-session
  [user candidate]
  (= (:session user) candidate))

(defn user->ctxts
  [user]
  (let [ctxts (:ctxts user)]
    (if (nil? ctxts)
      (if user
        ["c/wordnet" "c/web" (:id user)]
        ["c/wordnet" "c/web"])
      (clojure.string/split ctxts #" "))))
