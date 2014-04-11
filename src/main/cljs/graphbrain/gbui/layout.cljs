(ns graphbrain.gbui.layout
  (:require [graphbrain.gbui.mat :as mat]
            [graphbrain.gbui.globals :as g]
            [cemerick.pprng :as rng]))

(defn frand
  []
  (- (rng/double @g/rng) 0.5))

(defn all-pairs [coll]
  (when-let [s (next coll)]
    (lazy-cat (for [y s] [(first coll) y])
              (all-pairs s))))

(defn coulomb-energy
  [snodes]
  (let [x (apply + (map
                   #(/ 1.0 (mat/v3diff-length (:tpos (first %)) (:tpos (second %))))
                   (all-pairs snodes)))]
       x))

(defn force-pair
  [snodes pair]
  (let [i (first pair)
        j (second pair)
        snode-i (nth snodes i)
        snode-j (nth snodes j)
        pos-i (:pos snode-i)
        pos-j (:pos snode-j)
        r (map - pos-i pos-j)
        l (mat/v3length r)
        l  (/ 1.0 (* l l l))
        snode-i (assoc snode-i
                  :f (map #(+ %1 (* l %2)) (:f snode-i) r))
        snode-j (assoc snode-j
                  :f (map #(- %1 (* l %2)) (:f snode-j) r))]
    (assoc snodes i snode-i j snode-j)))

(defn forces
  [snodes]
  (let [n (count snodes)
        r [0 0 0]
        snodes (into [] (map #(assoc % :f [0 0 0]) snodes))
        index-pairs (all-pairs (range n))]
    (reduce #(force-pair %1 %2) snodes index-pairs)))

(defn rand-pos
  []
  (loop [pos nil
         l 0]
    (if (not= l 0)
      (map #(/ % l) pos)
      (let [pos (map (fn [x] (* 2 (frand))) (range 3))]
        (recur pos (mat/v3length pos))))))

(defn init-pos
  [snode]
  (let [pos (rand-pos)]
    (assoc snode :pos pos :tpos pos)))

(defn update-force
  [snode step]
  (let [f (:f snode)
        pos (:pos snode)
        tpos (:tpos snode)
        d (mat/v3dotv3 f pos)
        f (map #(- %1 (* %2 d)) f pos)
        tpos (map #(+ %1 (* %2 step)) pos f)
        l (mat/v3length tpos)
        tpos (map #(/ % l) tpos)]
    (assoc snode :f f :pos pos :tpos tpos)))

(defn layout
  [snodes]
  (let [n (count snodes)
        snodes (map #(init-pos %) snodes)]
    (loop [sn snodes
           e0 (coulomb-energy sn)
           k 20
           step 0.01]
      (if (or (>= 0 k) (< step 1e-10))
        sn
        (let [sn (forces sn)
              sn (map #(update-force % step) sn)
              e (coulomb-energy sn)
              success (< e e0)
              step (if success (* step 2.0) (/ step 2.0))
              e0 (if success e e0)
              sn (if success (map #(assoc % :pos (:tpos %)) sn) sn)]
          (recur sn e0 (dec k) step))))))
