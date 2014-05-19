(ns graphbrain.web.common
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.user :as user]
            [graphbrain.db.utils :as utils]))

(defn init-graph!
  []
  (def gbdb (utils/init-with-consensus!))
  #_(def gbdb (gb/gbdb)))

(defn get-user
  [response]
  (let
    [username (:value ((response :cookies) "username"))
     session (:value ((response :cookies) "session"))]
    (if (or (not username) (not session))
      nil
      (let [user-node (gb/username->vertex gbdb username)]
        (if user-node
          (if (user/check-session user-node session)
            user-node))))))

(defn get-code
  []
  (let
    [prog (gb/getv gbdb "prog/prog")]
    (if prog
      (:prog prog) "")))

(defn get-tests
  []
  (let
    [tests (gb/getv gbdb "text/tests")]
    (if tests
      (:text tests) "")))
