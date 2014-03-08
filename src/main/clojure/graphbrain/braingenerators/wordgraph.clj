(ns graphbrain.braingenerators.wordgraph)

(defn add-node
  [graph word]
  (assoc graph word #{}))

(defn add-edge
  [graph orig targ]
  (let [orig-links (graph orig)
        targ-links (graph targ)]
    (assoc graph orig (conj orig-links targ))))
