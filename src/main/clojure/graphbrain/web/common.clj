(ns graphbrain.web.common
  (:require [graphbrain.db.graph :as gb]
            [graphbrain.db.user :as user]))

(defonce graph (gb/graph))

(defn get-user
  [response]
  (let
    [username (:value ((response :cookies) "username"))
     session (:value ((response :cookies) "session"))]
    (if (or (not username) (not session))
      nil
      (let
        [user-node (gb/username->node graph username)]
        (if user-node
          (if (user/check-session user-node session)
            user-node))))))

(defn get-code
  []
  (let
    [prog (gb/getv graph "prog/prog")]
    (if prog
      (:prog prog) "")))

(defn get-tests
  []
  (let
    [tests (gb/getv graph "text/tests")]
    (if tests
      (:text tests) "")))
