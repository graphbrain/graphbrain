(ns graphbrain.db.perms
  (:require [graphbrain.db.gbdb :as gb]))

(defn has-perm?
  [gbdb user-id ctxt-id perm]
  (gb/exists-rel? gbdb
                  [(str "r/*" perm) user-id ctxt-id]
                  ctxt-id))

(defn is-admin?
  [gbdb user-id ctxt-id]
  (has-perm? gbdb user-id ctxt-id "admin"))

(defn is-editor?
  [gbdb user-id ctxt-id]
  (has-perm? gbdb user-id ctxt-id "editor"))

(defn grant-perm!
  [gbdb user-id ctxt-id perm]
  (gb/putrel! gbdb
              [(str "r/*" perm) user-id ctxt-id]
              ctxt-id))

(defn grant-admin!
  [gbdb user-id ctxt-id]
  (grant-perm! gbdb user-id ctxt-id "admin"))

(defn grant-editor!
  [gbdb user-id ctxt-id]
  (grant-perm! gbdb user-id ctxt-id "editor"))
