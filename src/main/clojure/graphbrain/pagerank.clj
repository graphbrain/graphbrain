(ns graphbrain.pagerank
  (:use graphbrain.utils
        graphbrain.graphtools))

(defn init-pr
  [graph]
  (add-field-all-nodes graph :pr 1.0))

(defn- pr-contrib
  [graph node]
  (let [v (graph node)
        pr (:pr v)
        l (double (count (:in v)))]
    (/ pr l)))

(defn- new-node-pr
  [graph node damp]
  (let [links (keys (:in node))
        prc (reduce + (map #(pr-contrib graph %) links))]
    (assoc node :newpr (+ (/ (- 1.0 damp) (double (count graph))) (* damp prc)))))

(defn- compute-new-pr
  [graph damp]
  (map-map-vals #(new-node-pr graph % damp) graph))

(defn- update-node-pr
  [node]
  (assoc node :pr (:newpr node)))

(defn- update-pr
  [graph]
  (map-map-vals update-node-pr graph))

(defn sorted-by-pr
  [graph]
  (into
   (sorted-map-by (fn [key1 key2]
                    (compare (:pr (graph key2)) (:pr (graph key1))))) graph))
(defn- pr-node-error
  [node]
  (let [nodev (second node)]
    (Math/abs (- (:pr nodev) (:newpr nodev)))))

(defn- pr-error
  [graph]
  (reduce max (map pr-node-error graph)))

(defn compute-pr
  [graph damp]
  (loop [g graph
         error 1]
    (if (< error 0.0001)
      (sorted-by-pr g)
      (let [g1 (compute-new-pr g damp)
            err (pr-error g1)]
        (prn err)
        (recur (update-pr g1) err)))))
