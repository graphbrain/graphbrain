(ns graphbrain.web.css.main
  (:require [garden.def :refer [defstylesheet defstyles]]
            [graphbrain.web.css.general :as general]
            [graphbrain.web.css.inters :as inters]
            [graphbrain.web.css.link :as link]
            [graphbrain.web.css.frame :as frame]
            [graphbrain.web.css.nodepage :as np]
            [graphbrain.web.css.bubble :as bubble]
            [graphbrain.web.css.item :as item]
            [graphbrain.web.css.eco :as eco]))

(defstyles main
  (concat general/css
          inters/css
          link/css
          frame/css
          np/css
          bubble/css
          item/css
          eco/css))
