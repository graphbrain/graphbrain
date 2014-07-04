(ns graphbrain.web.contexts
  (:require [graphbrain.web.colors :as colors]))

(defn active-ctxts
  [response user]
  (let [ctxts (:value ((response :cookies) "ctxts"))]
    (if (nil? ctxts)
      ["c/wordnet" "c/web" (:id user)]
      (clojure.string/split ctxts #":"))))

(defn color
  [ctxt-id user-id]
  (condp = ctxt-id
    "c/wordnet" "#332288"
    "c/web" "#88CCEE"
    user-id "#117733"
    (nth colors/colors (mod (Math/abs (.hashCode ctxt-id)) (count colors/colors)))))
