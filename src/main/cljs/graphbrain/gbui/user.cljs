(ns graphbrain.gbui.user
  (:require [jayq.core :as jq]
            [goog.net.cookies :as cookies])
  (:use [jayq.core :only [$]]))

(defonce auto-update-username (atom true))

(defonce username-status (atom "unknown"))

(defonce email-status (atom "unknown"))

(defonce submitting (atom false))

(defn show-signup-dialog!
  []
  (.modal ($ "#signUpModal") "show"))

(defn clear-signup-errors!
  []
  (.removeClass ($ "#nameFieldSet") "control-group error")
  (.removeClass ($ "#usernameFieldSet") "control-group error")
  (.removeClass ($ "#emailFieldSet") "control-group error")
  (.removeClass ($ "#passFieldSet") "control-group error")
  (.html ($ "#signupErrMsg") "")
  (.removeClass ($ "#logEmailFieldSet") "control-group error")
  (.removeClass ($ "#logPassFieldSet") "control-group error")
  (.html ($ "#loginErrMsg") ""))

(declare signup!)

(defn check-username-reply
  [msg]
  (let [response (clojure.string/split msg #" ")
        status (first response)
        username (second response)]
    (if (= username (.val ($ "#suUsername")))
      (if (= status "ok")
        (do
          (reset! username-status "ok")
          (.removeClass ($ "#usernameFieldSet") "control-group error")
          (.addClass ($ "#usernameFieldSet") "control-group success")
          (if @submitting (signup!)))
        (do
          (reset! username-status "exists")
          (.removeClass ($ "#usernameFieldSet") "control-group success")
          (.addClass ($ "#usernameFieldSet") "control-group error")
          (.html ($ "#signupErrMsg") "Sorry, this username is already in use.")
          (reset! submitting false))))))

(defn check-email-reply
  [msg]
  (.log js/console (str "check-email-reply> " msg))
  (let [response (clojure.string/split msg #" ")
        status (first response)
        email (second response)]
    (if (= email (.val ($ "#suEmail")))
      (if (= status "ok")
        (do
          (reset! email-status "ok")
          (.removeClass ($ "#emailFieldSet") "control-group error")
          (.addClass ($ "#emailFieldSet") "control-group success")
          (.html ($ "#emailErrMsg") "")
          (if @submitting (signup!)))
        (do
          (reset! email-status "exists")
          (.removeClass ($ "#emailFieldSet") "control-group success")
          (.addClass ($ "#emailFieldSet") "control-group error")
          (.html ($ "#signupErrMsg") "Sorry, this email is already in use.")
          (reset! submitting false))))))

(defn check-username!
  []
  (if (not (empty? (.val ($ "#suUsername"))))
    (jq/ajax {:type "POST"
              :url "/checkusername"
              :data (str "username=" (.val ($ "#suUsername")))
              :dataType "text"
              :success check-username-reply})))

(defn check-email!
  []
  (.log js/console "check-email!")
  (if (not (empty? (.val ($ "#suEmail"))))
    (jq/ajax {:type "POST"
              :url "/checkemail"
              :data (str "email=" (.val ($ "#suEmail")))
              :dataType "text"
              :success check-email-reply})))

(defn login-reply
  [msg]
  (if (= msg "failed")
    (.html ($ "#loginErrMsg") "Wrong username / email or password.")
    (let [response (.split msg " ")]
      (cookies/set "username" (first response) 8640000 "/")
      (cookies/set "session" (second response) 8640000 "/")
      (if (and (exists? js/data) (not (nil? js/data)))
        (.reload js/location)
        (set! (.-href js/window.location) (str "/node/user/" (first response)))))))

(defn login-request
  [log-email password]
  (clear-signup-errors!)
  (cond
      (empty? log-email) (do (.addClass ($ "#logEmailFieldSet")
                                        "control-group error")
                             (.html ($ "#loginErrMsg")
                                    "Email / Username cannot be empty.")
                             false)
      (empty? password) (do (.addClass ($ "#logPassFieldSet") "control-group error")
                            (.html ($ "#loginErrMsg") "Password cannot be empty.")
                            false)
      :else (do (jq/ajax {:type "POST"
                          :url "/login"
                          :data (str "login=" log-email "&password=" password)
                          :dataType "text"
                          :success login-reply})
                false)))

(defn signup-reply
  [msg]
  (login-request (.val ($ "#suEmail")) (.val ($ "#suPassword"))))

(defn signup!
  []
  (clear-signup-errors!)
  (let [name (.val ($ "#suName"))
        username (.val ($ "#suUsername"))
        email (.val ($ "#suEmail"))
        password (.val ($ "#suPassword"))
        password2 (.val ($ "#suPassword2"))
        filter #"^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$"]
    (.log js/console (str "signup!" @email-status " " @username-status))
    (cond
     (empty? name) (do (.addClass ($ "#nameFieldSet") "control-group error")
                       (.html ($ "#signupErrMsg") "Name cannot be empty.")
                       false)
     (empty? username) (do (.addClass ($ "#usernameFieldSet") "control-group error")
                           (.html ($ "#signupErrMsg") "Username cannot be empty.")
                           false)
     (empty? email) (do (.addClass ($ "#emailFieldSet") "control-group error")
                        (.html ($ "#signupErrMsg") "Email cannot be empty.")
                        false)
     (not (.test filter email)) (do (.addClass
                                     ($ "#emailFieldSet") "control-group error")
                                    (.html ($ "#signupErrMsg")
                                           "Not a valid email address.")
                                    false)
     (empty? password) (do (.addClass ($ "#passFieldSet") "control-group error")
                           (.html ($ "#signupErrMsg") "You must specify a password.")
                           false)
     (not= password password2) (do (.addClass ($ "#passFieldSet")
                                              "control-group error")
                                   (.html ($ "#signupErrMsg")
                                          "Passwords do not match.")
                                   false)
     (= @username-status "exists") false
     (= @username-status "unknown") (do (reset! submitting true)
                                        (check-username!)
                                        false)
     (= @email-status "exists") false
     (= @email-status "unknown") (do (reset! submitting true)
                                   (check-email!)
                                   false)
     :else (do (.log js/console "signup POST")
             (jq/ajax {:type "POST"
                         :url "/signup"
                         :data (str "name=" name "&username=" username "&email="
                                    email "&password=" password)
                         :dataType "text"
                         :success signup-reply})
               false))))

(defn login!
  []
  (let [log-email (.val ($ "#logEmail"))
        password (.val ($ "#logPassword"))]
    (login-request log-email password)))

(defn logout!
  []
  (cookies/set "username" "" 8640000 "/")
  (cookies/set "session" "" 8640000 "/")
  (.reload js/location))

(defn username-changed!
  [msg]
  (reset! auto-update-username false)
  (reset! username-status "unknown"))

(defn update-username!
  [msg]
  (if @auto-update-username
    (do (.val ($ "#suUsername") (clojure.string/replace
                                 (.toLowerCase (.val ($ "#suName"))) " " "_"))
        (reset! username-status "unknown"))))

(defn email-changed!
  [msg]
  (reset! email-status "unknown"))

(defn init-signup-dialog!
  []
  (let [html "<div class='modal hide' id='signUpModal' style='width:650px; height:500px; margin: -295px 0 0 -325px;'>  <div class='modal-header'>    <a class='close' data-dismiss='modal'>×</a>    <h3>Register or Login</h3>  </div>  <div class='modal-body' id='registerLoginBody' style='height:500px; overflow:hidden;'>   <div style='float:left'>      <h5>REGISTER NEW ACCOUNT</h5>      <span id='signupErrMsg' class='error' />      <form class='signupForm'>        <fieldset id='nameFieldSet'>          <label>Name</label>          <input id='suName' type='text' class='span3' placeholder='Or an alias if you prefer'>        </fieldset>        <fieldset id='usernameFieldSet'>          <label>Username</label>          <input id='suUsername' type='text' class='span3' placeholder='Unique identifier'>        </fieldset>        <fieldset id='emailFieldSet'>          <label>Email</label>          <input id='suEmail' type='text' class='span3' placeholder='Will not be seen by other members'>        </fieldset>        <fieldset id='passFieldSet'>          <label>Password</label>          <input id='suPassword' type='password' class='span3' placeholder='A good password'>          <br />          <input id='suPassword2' type='password' class='span3' placeholder='Confirm password'>        </fieldset>            <br />        <a id='signupButton' class='btn btn-primary'>Sign Up</a>      </form>    </div>    <div style='float:right'>     <h5>LOGIN</h5>      <span id='loginErrMsg' class='error' />      <form class='loginForm'>        <fieldset id='logEmailFieldSet'>          <label>Email or Username</label>          <input id='logEmail' type='text' class='span3'>        </fieldset>        <fieldset id='logPassFieldSet'>          <label>Password</label>          <input id='logPassword' type='password' class='span3'>        </fieldset>              <br />        <a id='loginButton' class='btn btn-primary' data-dismiss='modal'>Login</a></form></div></div></div>"]
    (.appendTo ($ html) "body")
    (jq/bind ($ "#signupButton") "click" signup!)
    (jq/bind ($ "#loginButton") "click" login!)
    (jq/bind ($ "#suName") "keyup" update-username!)
    (jq/bind ($ "#suName") "blur" check-username!)
    (jq/bind ($ "#suUsername") "keyup" username-changed!)
    (jq/bind ($ "#suUsername") "blur" check-username!)
    (jq/bind ($ "#suEmail") "keyup" email-changed!)
    (jq/bind ($ "#suEmail") "blur" check-email!)))
