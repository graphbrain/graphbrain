(ns graphbrain.gbui.snode
  (:require [jayq.core :as jq])
  (:use [jayq.core :only [$]]
        graphbrain.gbui.graph
        graphbrain.gbui.node
        graphbrain.gbui.spherical))

(defn create-snode-vis
  []
  {:pos (js/newv3)
   :angle [0 0]
   :x 0
   :y 0
   :z 0
   :rpos (array 0 0 0)
   :aux-vec (array 0 0 0)
   :f (js/newv3)
   :tpos (js/newv3)})

(defn move-to
  [snode-id x y z]
  (let [gv @graph-vis
        snodes-vis (:snodes gv)
        snode-vis (snodes-vis snode-id)
        snode-vis (assoc snode-vis :x x :y y :z z)
        aux-vec (array x y z)
        snode-vis (assoc snode-vis :aux-vec aux-vec)
        affin-mat (:affin-mat gv)
        rpos (:rpos snode-vis)
        dummy (js/m4x4mulv3 affin-mat aux-vec rpos)
        sc (spherical (:negative-stretch gv) (:mapping-power gv))
        rpos (:rpos snode-vis)
        sc (assoc sc :coords (into [] (map identity rpos)))
        sc (cartesian->spherical sc)
        sc (view-mappin sc)
        sc (spherical->cartesian sc)
        rpos (apply array (:coords sc))
        coords (:coords sc)
        angle-x (Math/atan2 (nth coords 1) (nth coords 2))
        angle-y (Math/atan2 (nth coords 0) (nth coords 2))
        spread 0.7
        x (+ (* (nth rpos 0) half-width spread) half-width)
        y (+ (nth rpos 1) (* (nth rpos 1) half-height spread) half-height)
        z (+ (nth rpos 2) (* (nth rpos 2) (Math/min half-width half-height) 0.8))
        rpos (array x y z)
        snode-vis (assoc snode-vis :rpos rpos :angle [angle-x angle-y])]
    (reset! graph-vis (assoc-in gv [:snodes snode-id] snode-vis))
    (if (and (not (js/isNaN x)) (not (js/isNaN y)) (not (js/isNaN z)))
      (let [transform-str  (str "translate3d("
                                (- x half-width)
                                "px,"
                                (y - this.halfHeight)
                                "px,"
                                z
                                "px)"
                                " scale("
                                scale
                                ")")
            sn-div ($ (str "#" snode-id))]
        (jq/css sn-div {:-webkit-transform transform-str})
        (jq/css sn-div {:-moz-transform transform-str})
        (if (< z 0)
          (let [opacity (/ (- 1) (* z 0.007))]
            (jq/css {:opacity opacity})
            (jq/css {:opacity 1})))))))

(defn apply-pos
  [snode-id]
  (let [snode-vis ((:snodes @graph-vis) snode-id)
        pos (:pos snode-vis)
        x (nth 0 pos)
        y (nth 1 pos)
        z (nth 2 pos)]
    (move-to snode-id x y z)))

(defn place
  [snode-id]
  (let [snode ((:snodes @graph) snode-id)
        rel-text (if (is-root snode-id)
                   ""
                   (label (:label snode) (:relpos snode)))
        html (if (is-root snode-id)
               (str "<div id='" snode-id "' class='snode'>")
               (str "<div id='" snode-id "' class='snodeR'>"))
        html (str html
                  "<div class='snodeLabel'>"
                  rel-text
                  "</div>"
                  "<div class='snodeInner'>"
                  "<div class='viewport' /></div></div>")]
    (jq/append ($ "#graph-view") html)
    (doseq [node (:nodes snode)]
      (node-place node snode false))
    (let [sn-div ($ (str "#" snode-id))]
      (if (> (jq/outerHeight sn-div) 250)
        (do
          (js/slimScroll ($ (str "#" snode-id " .viewport") {:height "250px"}))
          (js/hover sn-div js/scrollOn js/scrollOff)))
      (let [snode-vis ((:snodes @graph-vis) snode-id)
            width (jq/outerWidth sn-div)
            height (jq/outerHeight sn-div)
            snode-vis (assoc snode-vis :width width :height height)]
        (reset! graph-vis (assoc-in @graph-vis [:snodes snode-id] snode-vis)))
      (if (is-root snode-id)
        (set-color snode-id (:color snode))))))

(defn set-color
  [snode-id color]
    (jq/css ($ (str "#" snode-id)) {:border-color color})
    (jq/css ($ (str "#" snode-id " .snodeLabel")) {:background color}))
