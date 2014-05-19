(ns graphbrain.web.handlers.ecoedittests
  (:use (graphbrain.web common)
        (graphbrain.web.views ecopage ecoedittests))
  (:import (com.graphbrain.db TextNode)))

(defn handle-ecoedittests-get
  [request]
  (let
    [tests (get-tests)]
    (ecopage
      :title "Edit Tests"
      :body-fun (fn [] (ecoedittests-view tests)))))

(defn handle-ecoedittests-post
  [request]
  nil
  #_(. graph putOrUpdate
    (new TextNode "text/tests", ((request :form-params) "tests")))
  (handle-ecoedittests-get request))
