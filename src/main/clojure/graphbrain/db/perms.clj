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

(defn is-following?
  [gbdb user-id ctxt-id]
  (gb/exists-rel? gbdb
                  ["r/*following" user-id ctxt-id]
                  user-id))

(defn can-edit?
  [gbdb user-id ctxt-id]
  (or (is-editor? gbdb user-id ctxt-id)
      (is-admin? gbdb user-id ctxt-id)))

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

(defn follow!
  [gbdb user-id ctxt-id]
  (gb/putrel! gbdb
              ["r/*following" user-id ctxt-id]
              user-id))

(defn unfollow!
  [gbdb user-id ctxt-id]
  (gb/remrel! gbdb
              ["r/*following" user-id ctxt-id]
              user-id))

(defn toggle-follow!
  [gbdb user-id ctxt-id]
  (if (is-following? gbdb user-id ctxt-id)
    (unfollow! gbdb user-id ctxt-id)
    (follow! gbdb user-id ctxt-id)))
