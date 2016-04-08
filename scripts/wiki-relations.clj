(ns graphbrain.tools.script
  (:require [graphbrain.metrics.wiki :as wiki]))

(defn sample-vertex?
  [x]
  (let [vert (:vertex x)]
    (if (coll? vert)
      (if (= (first vert) "related/1")
        (if (and
             (not (= (beliefs/sources hg vert) #{"anon/enwiki_usr_spec"}))
             (or
              (not (ops/exists? hg (edge/negative vert)))
              (not
               (= (beliefs/sources hg (edge/negative vert)) #{"anon/enwiki_usr_spec"}))))
          (< (rand) 0.1))))))

(defn edge->id
  [edge]
  (clojure.string/replace edge "," "_"))

(defn mean-f
  [f s]
  (if (empty? s)
    0.0
    (/
     (reduce #(+ %1 (f %2)) 0.0 s)
     (float
      (count s)))))

(defn said-who
  [hg edge]
  (filter
   #(not (= % "anon/enwiki_usr_spec"))
   (beliefs/sources hg edge)))

(defn user-claims
  [user]
  (float (count
          (wiki/claims hg user))))

(defn user-refutations
  [user]
  (float (count
          (wiki/refutations hg user))))

(defn edited-by-user
  [user]
  (float (count
          (wiki/edited-by hg user))))

(defn print-row!
  [edge]
  (let [id (edge->id edge)
        v1 (edge 1)
        v2 (edge 2)
        nedge (edge/negative edge)
        neg (>
             (count (beliefs/sources hg nedge)) 0)
        for (count (beliefs/sources hg edge))
        against (count (beliefs/sources hg nedge))
        so (wiki/semantic-overlap hg v1 v2)
        eo (wiki/editor-overlap hg v1 v2)
        deg1 (count (wiki/neighbors hg v1))
        deg2 (count (wiki/neighbors hg v2))
        posdeg1 (count (wiki/related hg v1))
        posdeg2 (count (wiki/related hg v2))
        negdeg1 (count (wiki/not-related hg v1))
        negdeg2 (count (wiki/not-related hg v2))
        inposdeg1 (count (wiki/in-related hg v1))
        inposdeg2 (count (wiki/in-related hg v2))
        innegdeg1 (count (wiki/in-not-related hg v1))
        innegdeg2 (count (wiki/in-not-related hg v2))
        outposdeg1 (count (wiki/out-related hg v1))
        outposdeg2 (count (wiki/out-related hg v2))
        outnegdeg1 (count (wiki/out-not-related hg v1))
        outnegdeg2 (count (wiki/out-not-related hg v2))
        eds1 (count (wiki/editors hg v1))
        eds2 (count (wiki/editors hg v2))
        auths (said-who hg edge)
        nauths (said-who hg nedge)
        claims (mean-f user-claims auths)
        refutations (mean-f user-refutations auths)
        editedby (mean-f edited-by-user auths)
        nclaims (mean-f user-claims nauths)
        nrefutations (mean-f user-refutations nauths)
        neditedby (mean-f edited-by-user nauths)]
    (println
     (clojure.string/join ","
                          [id neg for against so eo deg1 deg2
                           posdeg1 posdeg2 negdeg1 negdeg2
                           inposdeg1 inposdeg2 innegdeg1 innegdeg2
                           outposdeg1 outposdeg2 outnegdeg1 outnegdeg2
                           eds1 eds2 claims refutations editedby
                           nclaims nrefutations neditedby]))))

(ops/f-all hg #(if (sample-vertex? %)
                 (print-row! (:vertex %))))
