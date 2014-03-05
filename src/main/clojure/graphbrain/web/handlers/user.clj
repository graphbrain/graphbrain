(ns graphbrain.web.handlers.user
  (:use (graphbrain.web common)))

(defn handle-signup
  [request]
  (let
    [name ((request :form-params) "name")
     username ((request :form-params) "username")
     email ((request :form-params) "email")
     password ((request :form-params) "password")]
    (. graph createUser username  name  email  password  "user")
    "ok"))

(defn handle-check-username
  [request]
  (let
    [username ((request :form-params) "username")]
    (if (. graph usernameExists username)
      (str "exists " username)
      (str "ok " username))))

(defn handle-check-email
  [request]
  (let
    [email ((request :form-params) "email")]
    (if (. graph emailExists email)
      (str "exists " email)
      (str "ok " email))))

(defn handle-login
  [request]
  (let
    [login ((request :form-params) "login")
     password ((request :form-params) "password")
     user (. graph attemptLogin login password)]
    (if user
      (str (. user getUsername) " " (. user getSession))
      "failed")))