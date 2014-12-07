(ns graphbrain.web.handlers.allusers
  (:use [clojure.string :only [join]]
        (graphbrain.web.views page raw))
  (:require [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.common :as common]
            [graphbrain.db.gbdb :as gb]))

(defn- raw-html
  [request]
  (let
    [users (gb/all-users common/gbdb)]
    (str "<h2>All Users</h2>"
      (str "<strong>Count:" (count users) "</strong><br /><br />")
      (join
        (map
          (fn [u] (str
                    "<a href='/node/user/"
                    (:username u) "'>"
                    (:username u) "</a> "
                    (:name u)
                    " "
                    (:email u)
                    " "
                    (:pwdhash u)
                    "<br />")) users)))))

(defn handle
  [request]
  (let [user (common/get-user request)]
    (page
      :title "all users"
      :css-and-js (css+js/css+js)
      :user user
      :page :allusers
      :body-fun (fn [] (raw-view (raw-html request)))
      :js "")))
