(ns graphbrain.gbui.relations
  (:require [jayq.core :as jq]
            [graphbrain.gbui.globals :as g]
            [graphbrain.gbui.snode :as snode]
            [graphbrain.gbui.animation :as anim])
  (:use [jayq.core :only [$]]))

(declare init-relations!)

(defn missing
  [rel]
  (nil? (get-in @g/graph [:snodes (:snode rel)])))

(defn nrel->html
  [nrel]
  (let [rel (first nrel)
        n (second nrel)
        label (snode/label (:label rel) (:pos rel))]
    (if (missing rel)
      (str "<a class='visible_rel_link' href='#' id='rel" n "'>" label "</a>")
      (str "<a class='hidden_rel_link' href='#' id='rel" n "'>" label "</a>"))))

(defn relation-reply
  [msg]
  (.log js/console "relation-reply!")
  (let [snode-data (g/add-snodes! msg)]
    (init-relations!)
    (let [sn (first snode-data)]
      (if (not (missing (first sn)))
        (anim/add-anim (anim/anim-lookat (second sn)))))))

;;  g.addSNodesFromJSON(msg);
;;  initRelations();
;;  sid = '';
;;  _ref = msg['snodes'];
;;  for (k in _ref) {
;;    v = _ref[k];
;;    sid = k;
;;  }
;;  if (sid !== '') {
;;    snode = g.snodes[sid];
;;    return addAnim(new AnimLookAt(snode));
;;  }
;;};

(defn relation-submit
  [msg]
  (let [event-data (.-data msg)]
    (if (nil? (get-in @g/graph [:snodes (:snode event-data)]))
      (js/ajax
       {:type "POST"
        :url "/rel"
        :data (str "rel="
                   (:rel event-data)
                   "&pos="
                   (:pos event-data)
                   "&rootId="
                   (:root @g/graph))
        :dataType "json"
        :success relation-reply})
      (anim/add-anim!
       (anim/anim-lookat (:snode event-data))))))

(defn init-relations!
  []
  (let [rels (:allrelations @g/graph)
        nrels (map #(vector %1 %2) rels (range (count rels)))
        html (clojure.string/join "<br />" (map nrel->html nrels))]
    (.html ($ "#rel-list") html)
    (loop [count 0
           rs rels]
      (if (empty? rs)
        nil
        (do
          (.bind ($ (str "#rel" count))
                    "click"
                    (first rs)
                    relation-submit)
          (recur (inc count) (rest rs)))))))
