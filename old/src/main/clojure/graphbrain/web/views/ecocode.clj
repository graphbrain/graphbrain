(ns graphbrain.web.views.ecocode
  (:use hiccup.core))

(def ^:private js "
var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {
  lineNumbers: true,
  styleActiveLine: true,
  matchBrackets: true,
  mode: \"eco\"
  });
")

(defn ecocode-view
  [code]
  (html
    [:form {:role "form" :action "code" :method "post"}
     [:textarea {:id "code" :name "code"} code]
     [:br]
     [:button {:type "submit" :class "btn btn-default"} "Save"]]
    [:script js]))