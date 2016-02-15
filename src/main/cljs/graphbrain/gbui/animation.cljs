;   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
;   All rights reserved.
;
;   Written by Telmo Menezes <telmo@telmomenezes.com>
;
;   This file is part of GraphBrain.
;
;   GraphBrain is free software: you can redistribute it and/or modify
;   it under the terms of the GNU Affero General Public License as published by
;   the Free Software Foundation, either version 3 of the License, or
;   (at your option) any later version.
;
;   GraphBrain is distributed in the hope that it will be useful,
;   but WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;   GNU Affero General Public License for more details.
;
;   You should have received a copy of the GNU Affero General Public License
;   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.

(ns graphbrain.gbui.animation
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.inters :as inters])
  (:use [jayq.core :only [$]]))

(def anims (atom []))

(def interval-id (atom nil))

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
  (reset! anims (doall (map run-cycle @anims)))
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

(defn anim-node-glow
  [node-div-id]
  {:name "nodeglow"
   :active true
   :stoppable false
   :node-div-id node-div-id
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
        r (Math/round (+ r1 (* (- r2 r1) x)))
        g (Math/round (+ g1 (* (- g2 g1) x)))
        b (Math/round (+ b1 (* (- b2 b1) x)))
        rgb-str (str "rgb(" r "," g "," b ")")
        active (< cycles 4)]
    (jq/css ($ (str "#" (:node-div-id anim))) {:background rgb-str})
    (assoc anim :delta delta :cycles cycles :x x :active active)))

(defn anim-graph-layout
  []
  {:name "graphlayout"
   :active true
   :stoppable false})

(defmethod run-cycle "graphlayout"
  [anim]
  (assoc anim
    :active (inters/layout-step!)))
