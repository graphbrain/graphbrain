(ns graphbrain.gbui.contexts
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [hiccups.runtime :as hiccupsrt]
            [goog.net.cookies :as cookies]
            [cljs.reader :as reader])
  (:use [jayq.core :only [$]]))

(defn- last-part
  [id]
  (let [ps (clojure.string/split id #"/")]
    (last ps)))

(defn- text
  [id]
  (clojure.string/replace (last-part id) "_" " "))

(defn- label
  [id]
  (let [desc (text id)]
    (clojure.string/join (cons (.toUpperCase (str (first desc))) (rest desc)))))

(defn id->ctxt
  [id]
  {:id id :name (label id)})

(defn active-ctxts
  []
  (let [ctxts (cookies/get "ctxts")
        user-id (str "u/" (cookies/get "username"))]
    (if (and (exists? ctxts) (not (nil? ctxts)))
      (clojure.string/split ctxts #":")
      ["c/wordnet" "c/web" user-id])))

(defn set-ctxts!
  [ctxts]
  (let [ctxts-str (clojure.string/join ":" ctxts)]
    (cookies/set "ctxts" ctxts-str 8640000 "/")))

(defn- ctxt-id
  [id]
  (clojure.string/replace id #"\W" "_"))

(defn- rem-id
  [id]
  (str "rem-" (ctxt-id id)))

(defn- add-id
  [id]
  (str "add-" (ctxt-id id)))

(hiccups/defhtml ctxt->html [ctxt]
  [:div {:class "dropdown dropup ctxt"}
    [:a {:href "#" :class "dropdown-toggle" :data-toggle "dropdown" :id ctxt} (label ctxt)]
    [:ul {:class "dropdown-menu" :role "menu" :aria-labelledby ctxt}
      [:li
        [:a {:href "#" :id (rem-id ctxt)} "Remove"]]
      [:li
        [:a {:href "#"} "Write"]]]])

(defn remove!
  [id]
  (let [ctxts (active-ctxts)
        ctxts (filter #(not (= % id)) ctxts)]
    (set-ctxts! ctxts))
  (.reload js/location))

(defn add!
  [id]
  (let [ctxts (active-ctxts)
        ctxts (if (some #{id} ctxts) ctxts (conj ctxts id))]
    (set-ctxts! ctxts))
  (.reload js/location))

(hiccups/defhtml ctxts->html []
  [:div {:class "dropdown dropup ctxt"}
    [:a {:href "#" :class "dropdown-toggle" :data-toggle "dropdown" :id "add-context-dropdown"} "+"]
    [:ul {:class "dropdown-menu" :role "menu" :aria-labelledby "add-context" :id "add-context"}]])

(defn init-add-context!
  [ctxts]
  (let [html (map #(str "<li><a href='#' id=" (add-id (:id %)) ">" (:name %) "</a></li>") ctxts)
        html (clojure.string/join html)]
    (.html ($ "#add-context") html))
  (doseq [ctxt ctxts]
      (jq/bind ($ (str "#" (add-id (:id ctxt)))) "click" #(add! (:id ctxt)))))

(defn init-contexts!
  []
  (let [ctxts (active-ctxts)
        html (clojure.string/join (map ctxt->html ctxts))
        html (str html (ctxts->html))]
    (.html ($ "#ctxt-area") html)
    (init-add-context! (:ctxts @g/graph))
    (doseq [ctxt ctxts]
      (jq/bind ($ (str "#" (rem-id ctxt))) "click" #(remove! ctxt)))))