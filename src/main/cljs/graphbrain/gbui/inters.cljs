(ns graphbrain.gbui.inters
  (:require [jayq.core :as jq]
            [graphbrain.gbui.bubble :as bubble]
            [graphbrain.gbui.link :as link]
            [graphbrain.gbui.globals :as g])
  (:use [jayq.core :only [$]]))

(def scale 1)

(def bubbs (atom {}))

(defn- index-combinations
  [n cnt]
  (lazy-seq
   (let [c (vec (cons nil (for [j (range 1 (inc n))] (+ j cnt (- (inc n)))))),
	 iter-comb
	 (fn iter-comb [c j]
	   (if (> j n) nil
	       (let [c (assoc c j (dec (c j)))]
		 (if (< (c j) j) [c (inc j)]
		     (loop [c c, j j]
		       (if (= j 1) [c j]
			   (recur (assoc c (dec j) (dec (c j))) (dec j)))))))),
	 step
	 (fn step [c j]
	   (cons (rseq (subvec c 1 (inc n)))
		 (lazy-seq (let [next-step (iter-comb c j)]
			     (when next-step (step (next-step 0) (next-step 1)))))))]
     (step c 1))))

(defn combinations
  "All the unique ways of taking n different elements from items"
  [items n]      
  (let [v-items (vec (reverse items))]
    (if (zero? n) (list ())
	(let [cnt (count items)]
	  (cond (> n cnt) nil
		(= n cnt) (list (seq items))
		:else
		(map #(map v-items %) (index-combinations n cnt)))))))

(defn- view-size
  []
  (let [view-view ($ "#inters-view")
        width (jq/width view-view)
        height (jq/height view-view)]
    [width height]))

(defn- place-bubbles!
  [bubbles]
  (reset! bubbs
          (reduce #(bubble/place-bubble! %1 %2) @bubbs bubbles)))

(defn- place-links!
  [links]
  (doseq [link links]
    (link/place-link! @bubbs link)))

(defn- update-links!
  [links]
  (doseq [link links]
    (link/update-pos! @bubbs link)))

(defn move-world!
  [pos scale]
  (let [half-size (map #(/ % 2) @g/view-size)
        half-wsize (map #(/ % 2) g/world-size)
        trans (map - half-size half-wsize)
        transform-str (str "translate(" (first trans) "px," (second trans) "px) "
                           "scale(" scale ")")
        view-div ($ "#inters-view")]
    (jq/css view-div {:width (str (first g/world-size) "px")
                      :height (str (second g/world-size) "px")
                      :transform transform-str})))

(defn on-scroll-world
  [event]
  (let [delta (.-wheelDelta (.-originalEvent event))]
    (def scale (* scale (Math/pow 0.9995 delta))))
  (move-world! [0 0] scale)
  false)

(defn bind-events!
  []
  (jq/bind ($ "#data-view") "mousewheel" on-scroll-world))

(defn init-view!
  [view-data-str]
  (reset! g/view-size (view-size))
  (move-world! [0 0] 1)
  (bind-events!)
  (def data (cljs.reader/read-string view-data-str))
  (place-bubbles! (:vertices data))
  (place-links! (:links data)))

(defn- coulomb-pair
  [bubbles pair]
  (let [id1 (first pair)
        id2 (second pair)
        b1 (bubbles id1)
        b2 (bubbles id2)
        pos1 (:pos b1)
        pos2 (:pos b2)
        delta (map - pos1 pos2)
        r2 (reduce #(+ %1 (* %2 %2)) 0 delta)
        k 200.0
        f (/ k r2)
        ang (apply #(.atan2 js/Math %2 %1) delta)
        f1 [(* f (.cos js/Math ang)) (* f (.sin js/Math ang))]
        f2 (map - f1)
        v1 (:v b1)
        v2 (:v b2)
        v1 (map + v1 f1)
        v2 (map + v2 f2)
        b1 (assoc b1 :v v1)
        b2 (assoc b2 :v v2)]
    #_(do (.log js/console "---------------------------")
        (.log js/console (str "pos 1> " pos1))
        (.log js/console (str "pos 2> " pos2))
        (.log js/console (str "delta> " delta))
        (.log js/console (str "r2> " r2))
        (.log js/console (str "ang> " ang))
        (.log js/console (str "f1> " f1)))
    (assoc bubbles
      id1 b1
      id2 b2)))

(defn- coulomb
    [bubbles]
    (let [pairs (combinations (keys bubbles) 2)]
      (reduce coulomb-pair bubbles pairs)))

(defn- center-attraction
  [bubbles id]
  (let [b (bubbles id)
        pos (:pos b)
        r2 (reduce #(+ %1 (* %2 %2)) 0 pos)
        k 0.000001
        f (* k r2)
        ang (apply #(.atan2 js/Math %2 %1) pos)
        f [(* f (.cos js/Math ang)) (* f (.sin js/Math ang))]
        v (:v b)
        v (map - v f)
        b (assoc b :v v)]
    (assoc bubbles id b)))

(defn- drag
  [bubbles id]
  (let [b (bubbles id)
        v (:v b)
        d 0.99
        v (map #(* d %) v)
        b (assoc b :v v)]
    (assoc bubbles id b)))

(defn- forces
  [bubbles]
  (let [bubbs (reduce drag bubbles (keys bubbles))
        bubbs (reduce center-attraction bubbs (keys bubbs))]
    bubbs))

(defn layout-step!
  []
  (reset! bubbs (coulomb @bubbs))
  (reset! bubbs (forces @bubbs))
  (let [keys (keys @bubbs)]
    (doseq [k keys]
      (reset! bubbs (bubble/layout-step! @bubbs k))))
  (update-links! (:links data))
  true)
