(ns graphbrain.web.contexts)

(defn user->ctxts
  [user]
  (let [ctxts (:ctxts user)]
    (if (nil? ctxts)
      ["c/wordnet" "c/web" (:id user)]
      (clojure.string/split ctxts #" "))))

(defn active-ctxts
  [response user]
  (let [ctxts (:value ((response :cookies) "ctxts"))]
    (if (nil? ctxts)
      ["c/wordnet" "c/web" (:id user) "k/emmanuel/psychosis"]
      (clojure.string/split ctxts #":"))))