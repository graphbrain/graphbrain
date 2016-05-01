(ns graphbrain.tools.script
  (:require [graphbrain.hg.ops :as ops]
            [graphbrain.hg.beliefs :as beliefs]
            [graphbrain.hg.symbol :as symb]
            [graphbrain.hg.constants :as const]
            [clojure.set :as set]
            [clojure.math.combinatorics :as combs]))

(def related
  (memoize
   (fn [hg symbol]
     (clojure.set/union
      (ops/pattern->edges hg ["related/1" nil symbol])
      (ops/pattern->edges hg ["related/1" symbol nil])
      (ops/pattern->edges hg ["related/1" symbol nil nil])
      (ops/pattern->edges hg ["related/1" nil symbol nil])
      (ops/pattern->edges hg ["related/1" nil nil symbol])))))

(def related2
  (memoize
   (fn [hg symbol]
     (clojure.set/union
      (ops/pattern->edges hg ["related/2" nil symbol])
      (ops/pattern->edges hg ["related/2" symbol nil])
      (ops/pattern->edges hg ["related/2" symbol nil nil])
      (ops/pattern->edges hg ["related/2" nil symbol nil])
      (ops/pattern->edges hg ["related/2" nil nil symbol])))))

(def not-related
  (memoize
   (fn [hg symbol]
     (clojure.set/union
      (ops/pattern->edges hg ["~related/1" nil symbol])
      (ops/pattern->edges hg ["~related/1" symbol nil])
      (ops/pattern->edges hg ["~related/1" symbol nil nil])
      (ops/pattern->edges hg ["~related/1" nil symbol nil])
      (ops/pattern->edges hg ["~related/1" nil nil symbol])))))

(def neighbors
  (memoize
   (fn [hg symbol]
     (disj
      (into #{}
            (flatten (clojure.set/union
                      (map rest (related hg symbol))
                      (map rest (not-related hg symbol)))))
      symbol))))

(def neighbors2
  (memoize
   (fn [hg symbol]
     (disj
      (into #{}
            (flatten (map
                      (fn [s] (map rest (related2 hg s)))
                      (neighbors hg symbol))))
      symbol))))

(def editors
  (memoize
   (fn [hg symbol]
     (let [rel (related hg symbol)
           sources (map #(beliefs/sources hg %) rel)]
       (into #{} sources)))))

(def edited-by
  (memoize
   (fn [hg symbol]
     (into #{}
           (map #(nth % 2) (ops/pattern->edges hg ["editor/1" symbol nil]))))))

(defn semantic-overlap
  [hg symb1 symb2]
  (let [n1 (neighbors hg symb1)
        n2 (neighbors hg symb2)]
    (/
     (float (count
             (clojure.set/intersection n1 n2)))
     (float (count
             (clojure.set/union n1 n2))))))

(defn semantic-overlap2
  [hg symb1 symb2]
  (let [n1 (neighbors2 hg symb1)
        n2 (neighbors2 hg symb2)]
    (/
     (float (count
             (clojure.set/intersection n1 n2)))
     (float (count
             (clojure.set/union n1 n2))))))

(defn editor-overlap
  [hg symb1 symb2]
  (let [n1 (editors hg symb1)
        n2 (editors hg symb2)]
    (/
     (float (count
             (clojure.set/intersection n1 n2)))
     (float (count
             (clojure.set/union n1 n2))))))

(def claims
  (memoize
   (fn [hg author]
     (filter
      #(= (first (second %))
          "related/1")
      (ops/pattern->edges hg [const/source nil author])))))

(def refutations
  (memoize
   (fn [hg author]
     (filter
      #(= (first (second %))
          "~related/1")
      (ops/pattern->edges hg [const/source nil author])))))



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
          (claims hg user))))

(defn user-refutations
  [user]
  (float (count
          (refutations hg user))))

(defn edited-by-user
  [user]
  (float (count
          (edited-by hg user))))

#_(defn print-row!
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

#_(ops/f-all hg #(if (sample-vertex? %)
                   (print-row! (:vertex %))))

(defn metrics
  [hg symb1 symb2]
  {:id symb1
   :eo (editor-overlap hg symb1 symb2)
   :so (semantic-overlap hg symb1 symb2)
   :degree (ops/degree hg symb1)})

(defn nmetrics
  [hg sym symbs]
  (let [mets (map #(metrics hg sym %) symbs)]
    {:id sym
     :eo (apply min (map :eo mets))
     :so (apply min (map :so mets))
     :degree (apply min (map :degree mets))}))

(defn neighbor-metrics
  [sym]
  (map #(metrics hg % sym)
       (neighbors hg "open_mind/enwiki")))

(defn top-n
  [tab n key]
  (take n
        (reverse (sort-by key tab))))

(defn concept-type
  [sym]
  (let [ns (symb/nspace sym)]
    (if (= ns "enwiki")
      "page"
      "header")))

(defn symbol->label
  [sym both]
  (let [s (clojure.string/replace
          (symb/symbol->str sym)
          "&" "\\&")
        s (if (> (count s) 20)
            (str (subs s 0 20) "... ")
            s)
        s (str s " (" (concept-type sym) ")")]
    (if (both sym)
      (str "\\textbf{" s "}")
      s)))

(defn print-latex-table!
  [so eo]
  (let [both (clojure.set/intersection
              (into #{} (map :id so))
              (into #{} (map :id eo)))]
    (println "\\footnotesize {")
    (println "\\begin{tabular}{ l | l | l || l | l | l}")
    (println "\\textbf{entity} & $\\alpha$ & $\\beta$ & \\textbf{entity} & $\\alpha$ & $\\beta$ \\\\")
    (println "\\hline")
    (doseq [i (range 25)]
      (println (str (symbol->label (:id (nth so i)) both) "&"
                    (format "%.3f" (:so (nth so i))) "&"
                    (format "%.3f" (:eo (nth so i))) "&"
                    (symbol->label (:id (nth eo i)) both) "&"
                    (format "%.3f" (:so (nth eo i))) "&"
                    (format "%.3f" (:eo (nth eo i)))
                    " \\\\"
                    )))
    (println "\\end{tabular}}")))


#_(println
 (let [nm (neighbor-metrics "open_mind/enwiki")
       n 25
       so (top-n nm n :so)
       eo (top-n nm n :eo)
       both (clojure.set/intersection
             (into #{} (map :id so))
             (into #{} (map :id eo)))]
   (println "\\footnotesize {")
   (println "\\begin{tabular}{ l | l | l || l | l | l}")
   (println "\\textbf{entity} & $\\alpha$ & $\\beta$ & \\textbf{entity} & $\\alpha$ & $\\beta$ \\\\")
   (println "\\hline")
   (doseq [i (range 25)]
     (println (str (symbol->label (:id (nth so i)) both) "&"
                   (format "%.3f" (:so (nth so i))) "&"
                   (format "%.3f" (:eo (nth so i))) "&"
                   (symbol->label (:id (nth eo i)) both) "&"
                   (format "%.3f" (:so (nth eo i))) "&"
                   (format "%.3f" (:eo (nth eo i)))
                   " \\\\"
                   )))
   (println "\\end{tabular}}")))

#_(println
 (clojure.set/intersection
  (neighbors hg "open_mind/enwiki")
  (neighbors hg "brain/enwiki")))




;;;; ===================================

(defn ns-type
  [sym]
  (let [nspace (symb/nspace sym)]
    (cond
     (= nspace "enwiki") :page
     (= nspace "enwikiusr") :user
     (and
      (>= (count nspace) 6)
      (= (subs nspace 0 6) "header")) :header
     (and
      (>= (count nspace) 2)
      (= (subs nspace 0 2) "WN")) :wordnet
     :else :misc)))

(defn accept?
  [sym]
  (#{:page :header :wordnet} (ns-type sym)))

(defn ego
  [hg center depth]
  (if (and (> depth 0) (accept? center))
    (do
      #_(println (str "ego> " center " " depth " " (ops/degree hg center)))
      (let [edges (ops/star hg center)
            ids (set (filter accept?
                             (flatten (map rest edges))))
            next-edges (map #(ego hg % (dec depth)) ids)]
        (apply set/union (conj next-edges edges))))))

(defn- add-neighbours
  [neighbours edge]
  (clojure.set/union neighbours
                     (set (rest edge))))

(defn- vemap-assoc-id-edge
  [vemap id edge]
  (if (accept? id)
    (let [vemap (update-in vemap [id :edges] #(conj % edge))
          vemap (update-in vemap [id :neighbours] add-neighbours edge)]
      vemap)
    vemap))

(defn- vemap+edge
  [vemap edge]
  (reduce #(vemap-assoc-id-edge %1 %2 edge)
          vemap
          (rest edge)))

(defn- edges->vemap
  [vemap edges]
  (reduce #(vemap+edge %1 %2) vemap edges))

(defn node->edges
  [hg node]
  (ego hg node 2))

(defn- interedge
  [edge interverts]
  (every? #(some #{%} interverts) (rest edge)))

(defn walks
  [vemap seeds walk all-walks]
  (if (> (count walk) 4)
    all-walks
    (if (every? #(some #{%} walk) seeds)
      (do
        #_(println walk)
        (conj all-walks walk))
     (let [neighbours (:neighbours (vemap (last walk)))
           next-steps (filter #(not (some #{%} walk)) neighbours)]
       (if (empty? next-steps)
         all-walks
         (reduce #(clojure.set/union %1
                                     (walks vemap seeds
                                            (conj walk %2)
                                            #{}))
                 all-walks next-steps))))))

(defn- walk-step->wmap
  [wmap walk-length step]
  (let [clen (wmap step)]
    (if (or (not clen) (< walk-length clen))
      (assoc wmap step walk-length)
      wmap)))

(defn- walk->wmap
  [wmap walk]
  (let [walk-length (count walk)]
    (reduce #(walk-step->wmap %1 walk-length %2)
            wmap walk)))

(defn- walks->wmap
  [walks]
  (reduce walk->wmap {} walks))

(defn- valid-step?
  [wmap nodes walk-length step]
  (or (some #{step} nodes)
      (= walk-length (wmap step))))

(defn- valid-walk?
  [wmap nodes walk]
  (let [walk-length (count walk)]
    (every? #(valid-step? wmap nodes walk-length %) walk)))

(defn intersect
  [hg seeds]
  #_(println (into [] (map #(node->edges hg %) seeds)))
  (let [edgesets (map #(node->edges hg %) seeds)
        vemap (reduce edges->vemap {} edgesets)
        walks (walks vemap seeds [(first seeds)] #{})
        wmap (walks->wmap walks)
        walks (filter #(valid-walk? wmap seeds %) walks)
        interverts (into #{}
                         (flatten
                          (into [] walks)))
        edges (vals vemap)
        edges (mapcat identity (map :edges edges))
        edges (filter #(interedge % interverts) edges)]
    {:walks walks
     :entities interverts
     :edges edges}))

(defn graph+pair
  [graph pair]
  (if-let [weight (graph pair)]
    (assoc graph pair (+ weight 1.0))
    (assoc graph pair 1.0)))

(defn graph+edge
  [graph edge]
  (reduce graph+pair
          graph
          (combs/combinations (rest edge) 2)))

(defn edges->graph
  [edges]
  (reduce graph+edge {} edges))

;; csv
#_(doseq [edge (edges->graph
              (intersect hg
                         ["open_mind/enwiki" "brain/enwiki"]))]
  (let [pair (first edge)
        orig (first pair)
        targ (second pair)
        weight (second edge)]
    (println (str orig "," targ "," weight))))


(let [seeds ["open_mind/enwiki" "brain/enwiki"]
      inter (intersect hg seeds)
      mets (map #(nmetrics hg % seeds) (:entities inter))
      so (top-n mets 25 :so)
      eo (top-n mets 25 :eo)]
  (print-latex-table! so eo))
