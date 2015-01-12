(ns graphbrain.gbui.user
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [goog.net.cookies :as cookies]
            [hiccups.runtime :as hiccupsrt])
  (:use [jayq.core :only [$]]))

(def initialised (atom false))

(def auto-update-username (atom true))

(def username-status (atom "unknown"))

(def email-status (atom "unknown"))

(def submitting (atom false))

(defn id
  []
  (str "u/"
       (cookies/get "username")))

(hiccups/defhtml signup-dialog-template
  []
  [:div {:class "modal" :role "dialog" :aria-hidden "true" :id "signup-modal"}
   [:div {:class "modal-dialog"}
    [:div {:class "modal-content"}
     [:div {:class "modal-header"}
      [:a {:class "close" :data-dismiss "modal"} "×"]
      [:h3 "Register or Login"]]
     [:div {:class "container-fluid"}
      [:div {:class "modal-body row" :id "registerLoginBody"}
       [:div {:class "col-md-6"}
        [:h5 "REGISTER NEW ACCOUNT"]
        [:span {:id "signupErrMsg" :class "text-danger"}]
        [:form {:role "form"}
         [:div {:id "name-formgroup" :class "form-group"}
          [:label {:class "control-label"} "Name"]
          [:input {:id "suName"
                   :type "text"
                   :class "form-control input-sm"
                   :placeholder "Or an alias if you prefer"}]]
         [:div {:id "username-formgroup" :class "form-group"}
          [:label {:class "control-label"} "Username"]
          [:input {:id "suUsername"
                   :type "text"
                   :class "form-control input-sm"
                   :placeholder "Unique identifier"}]]
         [:div {:id "email-formgroup" :class "form-group"}
          [:label {:class "control-label"} "Email"]
          [:input {:id "suEmail"
                   :type "text"
                   :class "form-control input-sm"
                   :placeholder "Will not be seen by other members"}]]
         [:div {:id "pass-formgroup" :class "form-group"}
          [:label {:class "control-label"} "Password"]
          [:input {:id "suPassword"
                   :type "password"
                   :class "form-control input-sm"
                   :placeholder "A good password"}]
          [:input {:id "suPassword2"
                   :type "password"
                   :class "form-control input-sm"
                   :placeholder "Confirm password"}]]
         [:br]
         [:a {:id "signupButton" :class "btn btn-primary"} "Sign Up"]]]
       [:div {:class "col-md-6"}
        [:h5 "LOGIN"]
        [:span {:id "loginErrMsg" :class "text-danger"}]
        [:form {:role "form"}
         [:div {:id "log-email-fieldgroup" :class "form-group"}
          [:label {:class "control-label"} "Email or Username"]
          [:input {:id "logEmail" :type "text" :class "form-control input-sm"}]]
         [:div {:id "log-pass-fieldgroup" :class "form-group"}
          [:label {:class "control-label"} "Password"]
          [:input {:id "logPassword"
                   :type "password"
                   :class "form-control input-sm"}]]
         [:br]
         [:a {:id "loginButton"
              :class "btn btn-primary"
              :data-dismiss "modal"} "Login"]]]]]
     [:div {:class "modal-footer"}]]]])

(defn clear-signup-errors!
  []
  (.removeClass ($ "#name-fieldgroup") "has-error")
  (.removeClass ($ "#username-fieldgroup") "has-error")
  (.removeClass ($ "#email-fieldgroup") "has-error")
  (.removeClass ($ "#pass-fieldgroup") "has-error")
  (.html ($ "#signupErrMsg") "")
  (.removeClass ($ "#log-email-fieldgroup") "has-error")
  (.removeClass ($ "#log-pass-fieldgroup") "has-error")
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
          (.removeClass ($ "#username-formgroup") "has-error")
          (.addClass ($ "#username-formgroup") "has-success")
          (if @submitting (signup!)))
        (do
          (reset! username-status "exists")
          (.removeClass ($ "#username-formgroup") "has-success")
          (.addClass ($ "#username-formgroup") "has-error")
          (.html ($ "#signupErrMsg") "Sorry, this username is already in use.")
          (reset! submitting false))))))

(defn check-email-reply
  [msg]
  (let [response (clojure.string/split msg #" ")
        status (first response)
        email (second response)]
    (if (= email (.val ($ "#suEmail")))
      (if (= status "ok")
        (do
          (reset! email-status "ok")
          (.removeClass ($ "#email-formgroup") "has-error")
          (.addClass ($ "#email-formgroup") "has-success")
          (.html ($ "#emailErrMsg") "")
          (if @submitting (signup!)))
        (do
          (reset! email-status "exists")
          (.removeClass ($ "#email-formgroup") "has-success")
          (.addClass ($ "#email-formgroup") "has-error")
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
        (set! (.-href js/window.location) (str "/n/u/" (first response)))))))

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
    (cond
     (empty? name) (do (.addClass ($ "#name-fieldgroup") "has-error")
                       (.html ($ "#signupErrMsg") "Name cannot be empty.")
                       false)
     (empty? username) (do (.addClass ($ "#username-fieldgroup") "has-error")
                           (.html ($ "#signupErrMsg") "Username cannot be empty.")
                           false)
     (empty? email) (do (.addClass ($ "#email-fieldgroup") "has-error")
                        (.html ($ "#signupErrMsg") "Email cannot be empty.")
                        false)
     (not (.test filter email)) (do (.addClass
                                     ($ "#email-fieldgroup") "has-error")
                                    (.html ($ "#signupErrMsg")
                                           "Not a valid email address.")
                                    false)
     (empty? password) (do (.addClass ($ "#pass-fieldgroup") "has-error")
                           (.html ($ "#signupErrMsg") "You must specify a password.")
                           false)
     (not= password password2) (do (.addClass ($ "#pass-fieldgroup") "has-error")
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
     :else (do (jq/ajax {:type "POST"
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
  (set! js/document.location.href "/"))

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
  (let [html (signup-dialog-template)]
    (.appendTo ($ html) "body")
    (jq/bind ($ "#signupButton") "click" signup!)
    (jq/bind ($ "#loginButton") "click" login!)
    (jq/bind ($ "#suName") "keyup" update-username!)
    (jq/bind ($ "#suName") "blur" check-username!)
    (jq/bind ($ "#suUsername") "keyup" username-changed!)
    (jq/bind ($ "#suUsername") "blur" check-username!)
    (jq/bind ($ "#suEmail") "keyup" email-changed!)
    (jq/bind ($ "#suEmail") "blur" check-email!)))

(defn show-signup-dialog!
  []
  (if (not @initialised)
    (do
      (init-signup-dialog!)
      (reset! initialised true)))
  (.modal ($ "#signup-modal") "show"))
