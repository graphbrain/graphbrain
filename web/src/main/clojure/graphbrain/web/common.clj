(ns graphbrain.web.common
  (:import (com.graphbrain.db Graph)))

(def graph (new Graph))

(defn get-user
  [response]
  (let
    [username (:username (response :cookies))
     session (:session (response :cookies))]
    (if (or (not username) (not session))
      nil
      (let
        [user-node (. graph getUserNodeByUsername username)]
        (if user-node
          (if (. user-node checkSession session)
            user-node))))))