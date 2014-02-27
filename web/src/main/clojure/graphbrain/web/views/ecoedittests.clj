(ns graphbrain.web.views.ecoedittests
  (:use hiccup.core))

(def ^:private js "
var editor = CodeMirror.fromTextArea(document.getElementById(\"tests\"), {
  lineNumbers: true,
  styleActiveLine: true,
  matchBrackets: true,
  mode: \"eco\"
  });
")

(defn ecoedittests-view
  [tests]
  (html
    [:form {:role "form" :action "edittests" :method "post"}
     [:textarea {:id "tests" :name "tests"} tests]
     [:br]
     [:button {:type "submit" :class "btn btn-default"} "Save"]]
    [:script js]))