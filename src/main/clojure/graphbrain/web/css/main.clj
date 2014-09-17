(ns graphbrain.web.css.main
  (:require [garden.def :refer [defstylesheet defstyles]]
            [graphbrain.web.css.general :as general]
            [graphbrain.web.css.bootstrap :as bootstrap]
            [graphbrain.web.css.node :as node]
            [graphbrain.web.css.snode :as snode]
            [graphbrain.web.css.contexts :as contexts]
            [graphbrain.web.css.aichat :as aichat]
            [graphbrain.web.css.navbar :as navbar]
            [graphbrain.web.css.relations :as relations]
            [graphbrain.web.css.view :as view]
            [graphbrain.web.css.bubble :as bubble]
            [graphbrain.web.css.frame :as frame]
            [graphbrain.web.css.nodepage :as np]))

(defstyles main
  (concat general/css
          bootstrap/css
          node/css
          snode/css
          contexts/css
          aichat/css
          navbar/css
          relations/css
          view/css
          bubble/css
          frame/css
          np/css))
