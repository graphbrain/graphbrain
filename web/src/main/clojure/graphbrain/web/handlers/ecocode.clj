(ns graphbrain.web.handlers.ecocode
  (:use (graphbrain.web common)
        (graphbrain.web.views ecopage ecocode))
  (:import (com.graphbrain.db ProgNode)))

(defn handle-ecocode-get
  [request]
  (let
    [code (get-code)]
    (ecopage
      :title "Code"
      :body-fun (fn [] (ecocode-view code)))))

(defn handle-ecocode-post
  [request]
  (. graph putOrUpdate
    (new ProgNode "prog/prog", ((request :form-params) "code")))
  (handle-ecocode-get request))