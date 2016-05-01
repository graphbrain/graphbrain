(ns graphbrain.tools.script
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.symbol :as symb]
            [clojure.set :as set]
            [clojure.math.combinatorics :as combs]))

(defn ns-type
  [sym]
  (let [nspace (symb/nspace sym)]
    (cond
     (= nspace "enwiki") :page
     (= nspace "enwikiusr") :user
     (and
      (>= (count nspace) 6)
      (= (subs nspace 0 6) "header")) :header
     (and
      (>= (count nspace) 2)
      (= (subs nspace 0 2) "WN")) :wordnet
     :else :misc)))

(defn accept?
  [sym]
  (#{:page :header :wordnet} (ns-type sym)))

(defn ego
  [hg center depth]
  (if (and (> depth 0) (accept? center))
    (do
      #_(println (str "ego> " center " " depth " " (ops/degree hg center)))
      (let [edges (ops/star hg center)
            ids (set (filter accept?
                             (flatten (map rest edges))))
            next-edges (map #(ego hg % (dec depth)) ids)]
        (apply set/union (conj next-edges edges))))))

(defn- add-neighbours
  [neighbours edge]
  (clojure.set/union neighbours
                     (set (rest edge))))

(defn- vemap-assoc-id-edge
  [vemap id edge]
  (if (accept? id)
    (let [vemap (update-in vemap [id :edges] #(conj % edge))
          vemap (update-in vemap [id :neighbours] add-neighbours edge)]
      vemap)
    vemap))

(defn- vemap+edge
  [vemap edge]
  (reduce #(vemap-assoc-id-edge %1 %2 edge)
          vemap
          (rest edge)))

(defn- edges->vemap
  [vemap edges]
  (reduce #(vemap+edge %1 %2) vemap edges))

(defn node->edges
  [hg node]
  (ego hg node 2))

(defn- interedge
  [edge interverts]
  (every? #(some #{%} interverts) (rest edge)))

(defn walks
  [vemap seeds walk all-walks]
  (if (> (count walk) 4)
    all-walks
    (if (every? #(some #{%} walk) seeds)
      (do
        #_(println walk)
        (conj all-walks walk))
     (let [neighbours (:neighbours (vemap (last walk)))
           next-steps (filter #(not (some #{%} walk)) neighbours)]
       (if (empty? next-steps)
         all-walks
         (reduce #(clojure.set/union %1
                                     (walks vemap seeds
                                            (conj walk %2)
                                            #{}))
                 all-walks next-steps))))))

(defn- walk-step->wmap
  [wmap walk-length step]
  (let [clen (wmap step)]
    (if (or (not clen) (< walk-length clen))
      (assoc wmap step walk-length)
      wmap)))

(defn- walk->wmap
  [wmap walk]
  (let [walk-length (count walk)]
    (reduce #(walk-step->wmap %1 walk-length %2)
            wmap walk)))

(defn- walks->wmap
  [walks]
  (reduce walk->wmap {} walks))

(defn- valid-step?
  [wmap nodes walk-length step]
  (or (some #{step} nodes)
      (= walk-length (wmap step))))

(defn- valid-walk?
  [wmap nodes walk]
  (let [walk-length (count walk)]
    (every? #(valid-step? wmap nodes walk-length %) walk)))

(defn intersect
  [hg seeds]
  #_(println (into [] (map #(node->edges hg %) seeds)))
  (let [edgesets (map #(node->edges hg %) seeds)
        vemap (reduce edges->vemap {} edgesets)
        walks (walks vemap seeds [(first seeds)] #{})
        wmap (walks->wmap walks)
        walks (filter #(valid-walk? wmap seeds %) walks)
        interverts (into #{}
                         (flatten
                          (into [] walks)))
        edges (vals vemap)
        edges (mapcat identity (map :edges edges))
        edges (filter #(interedge % interverts) edges)]
    edges))

(defn graph+pair
  [graph pair]
  (if-let [weight (graph pair)]
    (assoc graph pair (+ weight 1.0))
    (assoc graph pair 1.0)))

(defn graph+edge
  [graph edge]
  (reduce graph+pair
          graph
          (combs/combinations (rest edge) 2)))

(defn edges->graph
  [edges]
  (reduce graph+edge {} edges))

(doseq [edge (edges->graph
              (intersect hg
                         ["open_mind/enwiki" "brain/enwiki"]))]
  (let [pair (first edge)
        orig (first pair)
        targ (second pair)
        weight (second edge)]
    (println (str orig "," targ "," weight))))

