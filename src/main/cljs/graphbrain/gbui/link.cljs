(ns graphbrain.gbui.link
  (:require-macros [hiccups.core :as hiccups])
  (:require [jayq.core :as jq]
            [hiccups.runtime :as hiccupsrt]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.bubble :as bubble])
  (:use [jayq.core :only [$]]))

(defn link-id
  [link]
  (str "lnk_"
       (clojure.string/join "_"
                            (map bubble/bubble-id link))))

(hiccups/defhtml link-template
  [link]
  [:div {:id (link-id link)
         :class "link"}])

(defn update-pos!
  [bubbles link]
  (let [lid (link-id link)
        half-size (map #(/ % 2) g/world-size)
        bs (map #(bubbles (bubble/bubble-id %)) link)
        ps (map :pos bs)
        delta (apply #(map - %2 %1) ps)
        d (.sqrt js/Math
                 (reduce #(+ %1 (* %2 %2)) 0 delta))
        orig (apply #(map min %1 %2) ps)
        orig (first ps)
        half (map #(.abs js/Math (/ % 2.0)) delta)
        trans (map + orig half-size)
        ang (apply #(.atan2 js/Math %2 %1) delta)
        ang (- ang 1.5708)
        transform-str (str "translate(" (first trans) "px," (second trans) "px)"
                           " "
                           "rotate(" ang "rad)")
        height-str (str d "px")
        link-div ($ (str "#" lid))]
    (jq/css link-div {:height height-str
                      :transform-origin "0% 0%"
                      :transform transform-str
                      })))

(defn place-link!
  [bubbles link]
  (let [lid (link-id link)]
    (jq/append ($ "#inters-view")
               (link-template link))
    (update-pos! bubbles link)))
