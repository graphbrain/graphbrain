(ns graphbrain.web.common
  (:import (com.graphbrain.db Graph)))

(def graph (Graph.))

(defn get-user
  [response]
  (let
    [username (:value ((response :cookies) "username"))
     session (:value ((response :cookies) "session"))]
    (if (or (not username) (not session))
      nil
      (let
        [user-node (. graph getUserNodeByUsername username)]
        (if user-node
          (if (. user-node checkSession session)
            user-node))))))

(defn get-code
  []
  (let
    [prog (. graph getProgNode "prog/prog")]
    (if prog
      (. prog getProg) "")))

(defn get-tests
  []
  (let
    [tests (. graph getTextNode "text/tests")]
    (if tests
      (. tests getText) "")))
