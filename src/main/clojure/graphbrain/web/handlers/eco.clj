(ns graphbrain.web.handlers.eco
  (:require [graphbrain.db.gbdb :as gb]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.disambig.edgeguesser :as edg]
            [graphbrain.eco.eco :as eco]
            [graphbrain.eco.words :as words]
            [graphbrain.eco.parsers.chat :as chat]
            [graphbrain.web.common :as common]
            [graphbrain.web.contexts :as contexts]
            [graphbrain.web.cssandjs :as css+js]
            [graphbrain.web.views.eco :as ecop]))

(defn- js
  []
  (str "var ptype='eco';"))

(defn- sentence->result
  [user sentence ctxts]
  (let
      [env {:root "561852ced99a782d/europe"
            :user (:id user)}
       words (words/str->words sentence)
       par (eco/parse chat/chat words env)
       vws (map eco/vert+weight par)
       res (eco/verts+weights->vertex chat/chat vws env)]
    (if (id/edge? res)
      (let [edge-id (edg/guess common/gbdb res sentence (:id user) ctxts)
            edge (maps/id->vertex edge-id)
            edge (assoc edge :score 1)]
        {:words words
         :res res
         :vws vws
         :edge edge})
      {:words words
       :res res
       :vws vws})))

(defn report
  [user sentence ctxts]
  (sentence->result user sentence ctxts))

(defn handle
  [request]
  (prn (str "eco! " request))
  (let
      [sentence ((request :form-params) "input-field")
       user (common/get-user request)
       ctxts (contexts/active-ctxts (:id user) user)
       ctxt (contexts/context-data (:id user) (:id user))
       title "Eco test"
       report (if sentence
                (report user sentence ctxts))]
    (ecop/page :title title
               :css-and-js (css+js/css+js)
               :user user
               :ctxt ctxt
               :js (js)
               :report report)))
