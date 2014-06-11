(ns graphbrain.web.css.main
  (:require [garden.def :refer [defstylesheet defstyles]]
            [graphbrain.web.css.general :as general]
            [graphbrain.web.css.node :as node]
            [graphbrain.web.css.snode :as snode]))

(defstyles main
  (concat general/css node/css snode/css))
