(ns graphbrain.web.handlers.user
  (:use (graphbrain.web common)))

(defn handle-signup
  [response]
  (let
    [name ((response :form-params) "name")
     username ((response :form-params) "username")
     email ((response :form-params) "email")
     password ((response :form-params) "password")]
    (. graph createUser username  name  email  password  "user")
    "ok"))

(defn handle-check-username
  [response]
  (let
    [username ((response :form-params) "username")]
    (if (. graph usernameExists username)
      (str "exists " username)
      (str "ok " username))))

(defn handle-check-email
  [response]
  (let
    [email ((response :form-params) "email")]
    (if (. graph emailExists email)
      (str "exists " email)
      (str "ok " email))))

(defn handle-login
  [response]
  (let
    [login ((response :form-params) "login")
     password ((response :form-params) "password")
     user (. graph attemptLogin login password)]
    (if user
      (str (. user getUsername) " " (. user getSession))
      "failed")))