(ns graphbrain.web.visualvert
  (:require [graphbrain.db.gbdb :as db]
            [graphbrain.db.id :as id]
            [graphbrain.db.maps :as maps]
            [graphbrain.db.entity :as entity]
            [graphbrain.db.urlnode :as url]
            [graphbrain.db.text :as text]))

(defn id->visual
  [gbdb id ctxts]
  (let [vtype (id/id->type id)
        vert (maps/id->vertex id)
        vert (maps/local->global vert)]
    (case (:type vert)
      :entity (let [sub (entity/subentities vert)
                    sub (map #(hash-map :id (id/eid->id %)
                                        :text (entity/description %)) sub)]
                (assoc vert
                  :sub sub
                  :text (entity/label id)))
      :url (let [url (url/url vert)
                 title (url/title gbdb id ctxts)
                 title (if (empty? title) url title)
                 icon (str "http://www.google.com/s2/favicons?domain=" url)]
             (assoc vert
               :text title
               :url url
               :icon icon))
      :user (let [u (db/getv gbdb id ctxts)]
              {:id id
               :type :user
               :text (:name u)
               :sub [{:id "" :text "GraphBrain user"}]})
      :text (text/id->text gbdb id)
      (assoc vert
        :text id))))
