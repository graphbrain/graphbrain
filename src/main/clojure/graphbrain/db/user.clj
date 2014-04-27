(ns graphbrain.db.user
  (:require [graphbrain.db.id :as id])
  (:import (org.mindrot.jbcrypt BCrypt)
           (java.math BigInteger)
           (com.graphbrain.utils RandUtils)))

(defn id->user
  [id]
  {:id id
   :type :user
   :username (id/last-part id)
   :name ""
   :email ""
   :pwdhash ""
   :role ""
   :session ""
   :session-ts -1
   :last-seen -1
   :degree 0
   :ts -1})

(defn check-session
  [user candidate]
  (= (:session user) candidate))

(defn new-session
  [user]
  (assoc user :session (BigInteger. 130 (.toString (RandUtils/secRand) 32))))

(defn check-password
  [user candidate]
  (BCrypt/checkpw candidate (:pwdhash user)))

(defn check-session
  [user candidate]
  (= (:session user) candidate))
