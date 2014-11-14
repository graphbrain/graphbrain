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
            [graphbrain.web.css.inters :as inters]
            [graphbrain.web.css.bubble :as bubble]
            [graphbrain.web.css.link :as link]
            [graphbrain.web.css.frame :as frame]
            [graphbrain.web.css.nodepage :as np]
            [graphbrain.web.css.item :as item]))

(defstyles main
  (concat general/css
          bootstrap/css
          node/css
          snode/css
          contexts/css
          aichat/css
          navbar/css
          relations/css
          inters/css
          bubble/css
          link/css
          frame/css
          np/css
          item/css))
