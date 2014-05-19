(ns graphbrain.web.handlers.user
  (:require [graphbrain.db.gbdb :as gb])
  (:use (graphbrain.web common)))

(defn handle-signup
  [request]
  (let
    [name ((request :form-params) "name")
     username ((request :form-params) "username")
     email ((request :form-params) "email")
     password ((request :form-params) "password")]
    (gb/create-user! gbdb username name email password "user")
    "ok"))

(defn handle-check-username
  [request]
  (let
    [username ((request :form-params) "username")]
    (if (gb/username-exists? gbdb username)
      (str "exists " username)
      (str "ok " username))))

(defn handle-check-email
  [request]
  (let
    [email ((request :form-params) "email")]
    (if (gb/email-exists? gbdb email)
      (str "exists " email)
      (str "ok " email))))

(defn handle-login
  [request]
  (let
    [login ((request :form-params) "login")
     password ((request :form-params) "password")
     user (gb/attempt-login! gbdb login password)]
    (if user
      (str (:username user) " " (:session user))
      "failed")))
