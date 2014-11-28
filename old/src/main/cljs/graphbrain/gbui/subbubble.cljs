(ns graphbrain.gbui.subbubble
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(defn entity-id
  [bid id]
  (let [nid (clojure.string/replace id "/" "_")]
    (str "ent_" bid nid)))

(hiccups/defhtml entity-template
  [node bid]
  [:a {:href "#" :id (entity-id bid (:id node))} (:text node)])

(hiccups/defhtml subbubble-template
  [bid snode]
  [:div {:class "subbubble"}
   [:span {:class "subbubble-title"} (:label snode) ": "]
   (reduce #(str %1 ", " %2) (map #(entity-template % bid) (:nodes snode)))])

(defn html
  [bid snode-pair]
  (let [id (first snode-pair)
        snode (second snode-pair)]
    (subbubble-template bid snode)))

(defn on-mouse-down
  [id eid]
  (fn [event]
    (reset! g/origin {:id id})
    false))

(defn- bind-entity-events!
  [bid node]
  (let [id (:id node)
        eid (entity-id bid id)]
    (jq/bind ($ (str "#" eid)) "mousedown" (on-mouse-down id eid))))

(defn bind-events!
  [bid snode-pair]
  (doseq [entity (:nodes (second snode-pair))]
    (bind-entity-events! bid entity)))
