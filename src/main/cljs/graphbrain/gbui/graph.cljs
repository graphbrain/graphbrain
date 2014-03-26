(ns graphbrain.gbui.graph
  (:require [goog.dom :as dom]
            [goog.style :as style]))

(defrecord Graph
    [width
     height
     newedges
     half-width
     half-height
     snodes
     scale
     offset-x
     offset-y
     offset-z
     quat
     delta-quat
     affin-mat
     negative-stretch
     mapping-power
     changed-snode
     root-node-id])

(defn create-graph
  [width height newedges]
  (let [quat (Quaternion.)
        affin-mat (Array. 16)
        graph (Graph. width
                      height
                      newedges
                      (/ width 2)
                      (/ height 2)
                      {}
                      1
                      0
                      0
                      0
                      quat
                      (Quaternion.)
                      affin-mat
                      1
                      1
                      nil
                      false)]
    (. quat getMatrix affin-mat)
    graph))

(defn init-graph
  [newedges]
  (let [graph-view (dom/getElement "graph-view")
        size (style/getSize graph-view)
        width (.-width size)
        height (.-height size)
        graph (create-graph width height newedges)
        graph (update-transform graph)
        snode (SNode. graph "root" "" 0 "" "#000" true)
        graph (assoc-in graph [:snodes :root] snode)
        graph (assoc graph :root snode)
        nid  (aget (aget js/data "root") "id")
        root-node-id  nid
        text (aget (aget js/data "root") "text")
        text2 (aget (aget js/data "root") "text2")
        type (aget (aget js/data "root") "type")
        node (if (= type "url")
               (let [url (aget (aget js/data "root") "url")
                     icon (aget (aget js/data "root") "icon")]
                 (Node. nid text text2 type snode '' url icon))
               (Node. nid text text2 type snode ''))
        dummy (set! (.-root node) true)
        graph (assoc-in graph [:root :nodes nid] node)
        graph (assoc graph :rootNode node)
        dummy (. snode place)
        graph (add-snodes-From-json graph js/data)]
    graph))

(defn node-from-json
  [json]
  (let [nid (aget json "id")
        text (aget json "text")
        text2 (aget json "text2")
        type (aget json "type")
        edge (aget json "edge")]
    (if (= type "url")
      (let [url (aget json "url")
            icon (aget json "icon")]
        (Node. nid text text2 type snode edge url icon glow))
      (Node. nid text text2 type snode edge "" "" glow))))
               

(defn add-snodes-from-json
  [graph json]
  (let [snodes (aget json "snodes")]
    (loop [sns snodes
           g1 graph]
      (if (empty? sns)
        g1
        (let [sn (first sns)
              sid (first sn)
              v (second sn)]
          (let [label (aget v "label")]
            (if (and (!= label "x") (!= label "X"))
              (let [etype (aget v "etype")
                    rpos (aget v "rpos")
                    color (aget v "color")
                    nlist  (aget v "nodes")
                    snode (SNode. g1 sid etype rpos label color false)
                    g2 (assoc-in [:snodes sid] snode)
                    g2 (loop [nl nlist
                              g3 g2]
                      (if (empty? nl)
                        g3
                        (let [nod (first nl)
                              nid (aget nod "id")
                              text (aget nod "text")
                              text2 (aget nod "text2")
                              type (aget nod "type")
                              edge (aget nod "edge")
                              glow false
                              newedges (.-newedges graph)
                              g5 (if newedges
                                   (loop [nedges newedges
                                          g4 g3]
                                     (if (emtpy? nedges)
                                       g4
                                       (let [e (first nedges)]
                                         (if (and (!= e "") (!= e edge))
                                           (recur (rest nedges)
                                                  (assoc g4 :changed-snode snode))
                                           ;; glow = true;
                                           (recur (rest nedges) g4)))))
                                   g3)
                              node (if (= type "url")
                                     (Node. nid text text2 type snode edge nod['url'] nod['icon'] glow)
                                     (Node. nid text text2 type snode edge "" "" glow))
                              g5 (assoc-in g5 [:snodes sid :nodes nid] node)]
                          (snode-place snode)
                          (recur (rest nl) g5))
   this.layout()

  Graph.prototype.updateSize = function() {
    this.width = $('#graph-view').width();
    this.height = $('#graph-view').height();
    this.halfWidth = this.width / 2;
    return this.halfHeight = this.height / 2;
  };

  Graph.prototype.updateTransform = function() {
    var transformStr;
    transformStr = "translate(" + this.offsetX + "px," + this.offsetY + "px)" + " scale(" + this.scale + ")";
    $('#graph-view').css('-webkit-transform', transformStr);
    return $('#graph-view').css('-moz-transform', transformStr);
  };

  Graph.prototype.rotateX = function(angle) {
    this.deltaQuat.fromEuler(angle, 0, 0);
    this.quat.mul(this.deltaQuat);
    this.quat.normalise();
    return this.quat.getMatrix(this.affinMat);
  };

  Graph.prototype.rotateY = function(angle) {
    this.deltaQuat.fromEuler(0, 0, angle);
    this.quat.mul(this.deltaQuat);
    this.quat.normalise();
    return this.quat.getMatrix(this.affinMat);
  };

  Graph.prototype.zoom = function(deltaZoom, x, y) {
    var newScale, r, rx, ry;
    newScale = this.scale + (0.3 * deltaZoom);
    if (newScale < 0.4) {
      newScale = 0.4;
    }
    if (deltaZoom >= 0) {
      rx = x - this.halfWidth;
      this.offsetX = rx - (((rx - this.offsetX) / this.scale) * newScale);
      ry = y - this.halfHeight;
      this.offsetY = ry - (((ry - this.offsetY) / this.scale) * newScale);
    } else {
      if ((this.scale - 0.4) > 0) {
        r = (newScale - 0.4) / (this.scale - 0.4);
        this.offsetX *= r;
        this.offsetY *= r;
      }
    }
    this.scale = newScale;
    return this.updateTransform();
  };

  Graph.prototype.updateView = function() {
    var k, _results;
    _results = [];
    for (k in this.snodes) {
      _results.push(this.snodes[k].applyPos());
    }
    return _results;
  };

  Graph.prototype.layout = function() {
    var N, Nt, k, key, snodeArray;
    for (k in this.snodes) {
      this.snodes[k].initPosAndLayout();
    }
    this.root.moveTo(0, 0, 0);
    snodeArray = [];
    for (key in this.snodes) {
      if (this.snodes.hasOwnProperty(key) && !this.snodes[key].isRoot) {
        snodeArray.push(this.snodes[key]);
      }
    }
    layout(snodeArray);
    this.negativeStretch = 1;
    this.mappingPower = 1;
    N = snodeArray.length;
    Nt = 7;
    if (N > (Nt * 2)) {
      this.mappingPower = Math.log(Math.asin(Nt / (N / 2)) / Math.PI) * (1 / Math.log(0.5));
      this.negativeStretch = this.mappingPower * 2;
    }
    return this.updateView();
  };

  Graph.prototype.label = function(text, relpos) {
    if (relpos === 0) {
      return text + ' ' + this.rootNode['text'];
    } else {
      return text;
    }
  };

  return Graph;

})();
