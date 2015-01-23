(ns graphbrain.web.handlers.user
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.web.common :as common]))

(defn handle-signup
  [request]
  (let
    [name ((request :form-params) "name")
     username ((request :form-params) "username")
     email ((request :form-params) "email")
     password ((request :form-params) "password")]
    (common/log request (str "create user: " username))
    (gb/create-user! gbdb username name email password "user")
    "ok"))

(defn handle-check-username
  [request]
  (let
    [username ((request :form-params) "username")]
    (if (gb/username-exists? gbdb username)
      (do
        (common/log request (str "check username: " username "; EXISTS"))
        (str "exists " username))
      (do
        (common/log request (str "check username: " username "; OK"))
        (str "ok " username)))))

(defn handle-check-email
  [request]
  (let
    [email ((request :form-params) "email")]
    (if (gb/email-exists? gbdb email)
      (do
        (common/log request (str "check email: " email "; EXISTS"))
        (str "exists " email))
      (do
        (common/log request (str "check email: " email "; OK"))        
        (str "ok " email)))))

(defn handle-login
  [request]
  (let
    [login ((request :form-params) "login")
     password ((request :form-params) "password")
     user (gb/attempt-login! gbdb login password)]
    (if user
      (do
        (common/log request (str "login: " (:id user)))
        (str (:username user) " " (:session user)))
      (do
        (common/log request (str "FAILED LOGIN: " login))
        "failed"))))
