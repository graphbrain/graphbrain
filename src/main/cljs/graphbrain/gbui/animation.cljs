(ns graphbrain.gbui.animation
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.graph :as graph])
  (:use [jayq.core :only [$]]))

(defonce anims (atom []))

(defonce interval-id (atom nil))

(defn exclusive-anim?
  [anim]
  (let [name (:name anim)]
    (or (= name "initrotation") (= name "lookat"))))

(defn non-exclusive-anim?
  [anim]
  (not (exclusive-anim? anim)))

(defn- clear-interval!
  []
  (js/clearInterval @interval-id)
  (reset! interval-id nil))

(defn- remove-inactive!
  []
  (let [new-anims (filter #(:active %) @anims)]
    (reset! anims new-anims)
    (if (empty? @anims) (clear-interval!))))

(defmulti run-cycle :name)

(defn- anim-cycle
  []
  (reset! anims (map run-cycle @anims))
  (remove-inactive!))

(defn- set-interval!
  []
  (reset! interval-id
          (js/setInterval anim-cycle 30)))

(defn add-anim!
  [anim]
  (let [new-anims (if (= (:name anim) "lookat")
                    (filter non-exclusive-anim? @anims)
                    @anims)
        new-anims (conj new-anims anim)
        restart (empty? @anims)]
    (reset! anims new-anims)
    (if restart (set-interval!))))

(defn- stop-anims!
  []
  (let [new-anims (filter #(not (:stoppable %)) @anims)]
    (reset! anims new-anims)
    (if (empty? @anims) clear-interval!)))

(defn anim-init-rotation
  []
  {:name "initrotation"
   :active true
   :stoppable true
   :anim-speed [0.007 0.005]})

(defmethod run-cycle "initrotation"
  [anim]
  (let [speed (:anim-speed anim)
        speed-x (first speed)
        speed-y (second speed)]
    (graph/rotate (- speed-x) speed-y 0)
    (graph/update-view)
    (let [new-speed-x (* speed-x 0.98)
          new-speed-y (* speed-y 0.98)
          new-speed [new-speed-x new-speed-y]
          active (> new-speed-x 0.0001)
          new-anim (assoc anim :anim-speed new-speed)
          new-anim (assoc new-anim :active active)]
      new-anim)))

(defn anim-lookat
  [target-snode-id]
  {:name "lookat"
   :active true
   :stoppable true
   :target-snode-id target-snode-id})
    
(defmethod run-cycle "lookat"
  [anim]
  (let [speed-factor 0.05
        precision 0.01
        target-snode ((:snodes @g/graph-vis) (:target-snode-id anim))
        angle (:angle target-snode)
        angle-x (first angle)
        angle-y (second angle)
        speed-x (* angle-x speed-factor)
        speed-y (* angle-y speed-factor)]
    (graph/rotate-x speed-y)
    (graph/rotate-y (- speed-x))
    (graph/update-view)
    (let [target-snode ((:snodes @g/graph-vis) (:target-snode-id anim))
          angle (:angle target-snode)
          abs-angle-x (Math/abs (first angle))
          abs-angle-y (Math/abs (second angle))
          active (or (> abs-angle-x precision)
                     (> abs-angle-y precision))]
      (assoc anim :active active))))

(defn anim-node-glow
  [node-id]
  {:name "nodeglow"
   :active true
   :stoppable false
   :node-id node-id
   :x 0
   :cycles 0
   :delta 0.05
   :color1 [224.0 224.0 224.0]
   :color2 [189.0 218.0 249.0]})

(defmethod run-cycle "nodeglow"
  [anim]
  (let [delta (:delta anim)
        cycles (:cycles anim)
        x (+ (:x anim) delta)
        over-max (> x 1)
        x (if over-max 1 x)
        delta (if over-max (- delta) delta)
        below-min (< x 0)
        x (if below-min 0 x)
        delta (if below-min (- delta) delta)
        cycles (if below-min (inc cycles) cycles)
        color1 (:color1 anim)
        color2 (:color2 anim)
        r1 (nth color1 0)
        g1 (nth color1 1)
        b1 (nth color1 2)
        r2 (nth color2 0)
        g2 (nth color2 1)
        b2 (nth color2 2)
        r (Math/round (+ r1 ((- r2 r1) x)))
        g (Math/round (+ g1 ((- g2 g1) x)))
        b (Math/round (+ b1 ((- b2 b1) x)))
        rgb-str (str "rgb(" r "," g "," b ")")
        node ((:nodes @graph/graph) (:node-id anim))
        divid (:divid node)
        active (< cycles 4)]
    (jq/css ($ (str "#" divid)) {:background rgb-str})
    (assoc anim :delta delta :cycles cycles :x x :active active)))
