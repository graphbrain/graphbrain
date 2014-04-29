(ns graphbrain.web.handlers.user
  (:require [graphbrain.db.graph :as gb])
  (:use (graphbrain.web common)))

(defn handle-signup
  [request]
  (let
    [name ((request :form-params) "name")
     username ((request :form-params) "username")
     email ((request :form-params) "email")
     password ((request :form-params) "password")]
    (gb/create-user! graph username name email password "user")
    "ok"))

(defn handle-check-username
  [request]
  (let
    [username ((request :form-params) "username")]
    (if (gb/username-exists? graph username)
      (str "exists " username)
      (str "ok " username))))

(defn handle-check-email
  [request]
  (let
    [email ((request :form-params) "email")]
    (if (gb/email-exists? graph email)
      (str "exists " email)
      (str "ok " email))))

(defn handle-login
  [request]
  (let
    [login ((request :form-params) "login")
     password ((request :form-params) "password")
     user (gb/attempt-login! graph login password)]
    (if user
      (str (:username user) " " (:session user))
      "failed")))
