(ns graphbrain.hg.knowledge
  (:require [graphbrain.hg.ops :as hgops]
            [graphbrain.hg.id :as id]))

(defn- edge->def
  [edge defs]
  #_(if (= (maps/edge-type edge)
         "r/is")
    (let [parts (maps/participant-ids edge)]
      (assoc defs
        (id/eid->name (first parts))
        (second parts)))
    defs))

(defn- edges->defs
  [edges]
  (reduce #(edge->def %2 %1) {} edges))

(defn- apply-defs-edge
  [defs edge]
  #_(maps/ids->edge
   (map #(let [name (id/eid->name %)]
           (if (and
                (defs name)
                (id/undefined-eid? %))
             (id/name+ids->eid "r/+t"
                               name
                               [(defs name)])
             %))
        (maps/ids edge))))

(defn- apply-defs
  [defs edges]
  (map #(apply-defs-edge defs %)
       edges))

(defn- pre-process-edges
  [edges]
  (apply-defs
   (edges->defs edges) edges))

(defn addfact!
  [gbdb edge ctxt-id author-id]
  #_(if (= (maps/edge-type edge) "r/*edges")
    (doseq [e (pre-process-edges
               (map maps/id->edge
                    (maps/participant-ids
                     edge)))]
      (addfact! gbdb e ctxt-id author-id))
    (if (not (gb/getv gbdb (:id edge) [ctxt-id]))
      (let [fact-author-id ["r/*author" (:id edge) author-id]]
        (gb/putv! gbdb edge ctxt-id)
        (gb/putv! gbdb
                  (maps/id->edge
                   (id/ids->id fact-author-id))
                  ctxt-id)))))

(defn author
  [gbdb edge-id ctxts]
  #_(let [authors (gb/pattern->edges gbdb ["r/*author" edge-id "*"] ctxts)]
    (if authors
      (second
       (maps/participant-ids
        (first authors))))))
