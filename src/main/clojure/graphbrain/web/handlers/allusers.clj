(ns graphbrain.web.handlers.allusers
  (:use [clojure.string :only [join]]
        (graphbrain.web common)
        (graphbrain.web.views page raw))
  (:require [graphbrain.web.cssandjs :as css+js]))

(defn- raw-html
  [request]
  (let
    [users (. graph allUsers)]
    (str "<h2>All Users</h2>"
      (str "<strong>Count:" (count users) "</strong><br /><br />")
      (join
        (map
          (fn [u] (str
                    "<a href='/node/user/"
                    (. u getUsername) "'>"
                    (. u getUsername) "</a> "
                    (. u getName)
                    " "
                    (. u getEmail)
                    " "
                    (. u getPwdhash)
                    "<br />")) users)))))

(defn handle-allusers
  [request]
  (let [user (get-user request)]
    (page
      :title "all users"
      :css-and-js (css+js/css+js)
      :user user
      :page :allusers
      :body-fun (fn [] (raw-view (raw-html request)))
      :js "")))
