(ns graphbrain.web.contexts
  (:require [graphbrain.db.id :as id]
            [graphbrain.web.colors :as colors]))

(defn context-data
  [id user-id]
  (let [ctxt (id/context id)
        ctxt (if ctxt ctxt user-id)
        name (if (= ctxt user-id)
               "Personal"
               ctxt)]
    {:id ctxt
     :name name}))

(defn active-ctxts
  [response user]
  (let [ctxts (:value ((response :cookies) "ctxts"))]
    (if (nil? ctxts)
      (if user
        ["c/wordnet" "c/web" (:id user)]
        ["c/wordnet" "c/web"])
      (clojure.string/split ctxts #":"))))

(defn color
  [ctxt-id user-id]
  (condp = ctxt-id
    "c/wordnet" "#332288"
    "c/web" "#88CCEE"
    user-id "#117733"
    (nth colors/colors (mod (Math/abs (.hashCode ctxt-id)) (count colors/colors)))))
