(ns graphbrain.gbui.snode
  (:require [jayq.core :as jq]
            [graphbrain.gbui.spherical :as spher]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.node :as node]
            [graphbrain.gbui.mat :as mat]
            [graphbrain.gbui.newedges :as newedges])
  (:use [jayq.core :only [$]]))

(defn is-root
  [snode-id]
  (= snode-id "root"))

(defn label
  [text relpos]
  (if (= relpos 0)
    (str text " " (:text (:rootNode @g/graph-vis)))
    text))

(defn create-snode-vis
  []
  {:pos [0 0 0]
   :size [0 0]
   :angle [0 0]
   :x 0
   :y 0
   :z 0
   :rpos [0 0 0]
   :f [0 0 0]
   :tpos [0 0 0]})

(defn half-size
  [vis]
  (let [size (:size vis)
        width (first size)
        height (second size)]
    [(/ width 2.0) (/ height 2.0)]))

(defn move-to
  [snode-id x y z]
  (let [gv @g/graph-vis
        snodes-vis (:snodes gv)
        snode-vis (snodes-vis snode-id)
        snode-vis (assoc snode-vis :x x :y y :z z)
        aux-vec [(:x snode-vis) (:y snode-vis) (:z snode-vis)]
        snode-vis (assoc snode-vis :x x :y y :z z)
        affin-mat (:affin-mat gv)
        rpos (mat/m4x4mulv3 affin-mat aux-vec)
        sc (spher/spherical (:negative-stretch gv) (:mapping-power gv))
        sc (assoc sc :coords rpos)
        sc (spher/cartesian->spherical sc)
        sc (spher/view-mapping sc)
        sc (spher/spherical->cartesian sc)
        rpos (:coords sc)
        coords (:coords sc)
        angle-x (Math/atan2 (nth coords 1) (nth coords 2))
        angle-y (Math/atan2 (nth coords 0) (nth coords 2))
        spread 0.7
        half (half-size @g/graph-vis)
        half-width (first half)
        half-height (second half)
        nx (+ (* (nth rpos 0) half-width spread) half-width)
        ny (+ (nth rpos 1) (* (nth rpos 1) half-height spread) half-height)
        nz (+ (nth rpos 2) (* (nth rpos 2) (Math/min half-width half-height) 0.8))
        rpos [nx ny nz]
        snode-vis (assoc snode-vis :rpos rpos :angle [angle-x angle-y])
        nz (+ nz (nth (:offset @g/graph-vis) 2))]
    (reset! g/graph-vis (assoc-in gv [:snodes snode-id] snode-vis))
    (if (and (not (js/isNaN nx)) (not (js/isNaN ny)) (not (js/isNaN nz)))
      (let [s-half (half-size snode-vis)
            transform-str  (str "translate3d("
                                (- nx (first s-half))
                                "px,"
                                (- ny (second s-half))
                                "px,"
                                nz
                                "px)")
            sn-div ($ (str "#" snode-id))]
        (jq/css sn-div {:-webkit-transform transform-str})
        (jq/css sn-div {:-moz-transform transform-str})
        (if (< nz 0)
          (let [opacity (/ (- 1) (* nz 0.007))]
            (jq/css sn-div {:opacity opacity})
            (jq/css sn-div {:opacity 1})))))))

(defn apply-pos
  [snode-id]
  (let [snode-vis ((:snodes @g/graph-vis) snode-id)
        pos (:pos snode-vis)
        x (nth pos 0)
        y (nth pos 1)
        z (nth pos 2)]
    (move-to snode-id x y z)))

(defn set-color
  [snode-id color]
    (jq/css ($ (str "#" snode-id)) {:border-color color})
    (jq/css ($ (str "#" snode-id " .snode-label")) {:background color}))

(defn place
  [snode-id]
  (let [snode ((:snodes @g/graph) snode-id)
        relpos (:rpos snode)
        rel-text (if (is-root snode-id)
                   ""
                   (label (:label snode) relpos))
        html (if (is-root snode-id)
               (str "<div id='" snode-id "' class='snode-root'>")
               (str "<div id='" snode-id "' class='snode'>"))
        html (if (= relpos 0)
               (str html
                    "<div class='snode-inner'>"
                    "<div class='viewport' /></div>"
                    "<div class='snode-label'>"
                    rel-text
                    "</div></div>")
               (str html
                    "<div class='snode-label'>"
                    rel-text
                    "</div>"
                    "<div class='snode-inner'>"
                    "<div class='viewport' /></div></div>"))]
    (jq/append ($ "#graph-view") html)
    (doseq [node (:nodes snode)]
      (let [new-edge (some #{(:edge node)} (newedges/new-edges))]
        (if new-edge (reset! g/changed-snode snode-id))
        (node/node-place node snode-id snode (is-root snode-id))))
    (let [sn-div ($ (str "#" snode-id))]
      #_(if (> (.outerHeight sn-div) 250)
        (do
          (js/slimScroll ($ (str "#" snode-id " .viewport") {:height "250px"}))
          (js/hover sn-div js/scrollOn js/scrollOff)))
      (let [snode-vis ((:snodes @g/graph-vis) snode-id)
            width (.outerWidth sn-div)
            height (.outerHeight sn-div)
            snode-vis (assoc snode-vis :size [width height])]
        (reset! g/graph-vis (assoc-in @g/graph-vis [:snodes snode-id] snode-vis)))
      (if (not (is-root snode-id))
        (set-color snode-id (:color snode))))))
