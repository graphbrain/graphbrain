(ns graphbrain.gbui.remove
  (:require [jayq.core :as jq])
  (:use [jayq.core :only [$]]))

(defn remove-action
  []
  (.submit ($ "#removeForm")))

(defn init-remove-dialog
  [root-node-id]
  (let [html (str "<div class=\"modal hide\" id=\"removeModal\">"
                  "<div class=\"modal-header\">"
                  "<a class=\"close\" data-dismiss=\"modal\">×</a>"
                  "<h3>Confirm Removal</h3>"
                  "</div>"
                  "<form id=\"removeForm\" action='/node/"
                  root-node-id
                  "' method=\"post\">"
                  "<input type=\"hidden\" name=\"op\" value=\"remove\">"
                  "<input id=\"removeEdgeField\" type=\"hidden\" name=\"edge\">"
                  "<div class=\"modal-body\" id=\"addBrainBody\">"
                  "<div id=\"linkDesc\"></div>"
                  "</div>"
                  "<div class=\"modal-footer\">"
                  "<a class=\"btn\" data-dismiss=\"modal\">Close</a>"
                  "<a id=\"removeDlgButton\" class=\"btn btn-primary\">Remove</a>"
                  "</div></form></div>")]
    (jq/append ($ "body") html)
    (jq/bind ($ "#removeDlgButton") :click remove-action)))

(defn show-remove-dialog
  [node snode]
  (let [edge (:edge node)
        link (:label snode)
        html (str (:text node) " <strong>(" link "</strong>)")]
    (jq/val ($ "#removeEdgeField") edge)
    (jq/html ($ "#linkDesc") html)
    (.modal ($ "#removeModal") "show")))

(defn remove-clicked
  [node snode]
  (show-remove-dialog node snode))
