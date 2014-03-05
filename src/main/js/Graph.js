var Graph, rootNodeId;

rootNodeId = false;

Graph = (function() {

  function Graph(width, height, newedges) {
    this.width = width;
    this.height = height;
    this.newedges = newedges;
    this.halfWidth = width / 2;
    this.halfHeight = height / 2;
    this.snodes = {};
    this.scale = 1;
    this.offsetX = 0;
    this.offsetY = 0;
    this.zOffset = 0;
    this.quat = new Quaternion();
    this.deltaQuat = new Quaternion();
    this.affinMat = new Array(16);
    this.quat.getMatrix(this.affinMat);
    this.negativeStretch = 1;
    this.mappingPower = 1;
    this.changedSNode = null;
  }

  Graph.initGraph = function(newedges) {
    var graph, nid, node, snode, text, text2, type;
    graph = new Graph($('#graph-view').width(), $('#graph-view').height(), newedges);
    graph.updateTransform();
    snode = new SNode(graph, 'root', '', 0, '', '#000', true);
    graph.snodes['root'] = snode;
    graph.root = snode;
    nid = data['root']['id'];
    rootNodeId = nid;
    text = data['root']['text'];
    text2 = data['root']['text2'];
    type = data['root']['type'];
    if (type === 'url') {
      node = new Node(nid, text, text2, type, snode, '', data['root']['url'], data['root']['icon']);
    }
    else {
      node = new Node(nid, text, text2, type, snode, '');
    }
    node.root = true;
    snode.nodes[nid] = node;
    graph.rootNode = node;
    snode.place();
    graph.addSNodesFromJSON(data);
    return graph;
  };

  Graph.prototype.addSNodesFromJSON = function(json) {
    var color, e, edge, etype, glow, k, label, nid, nlist, nod, node, rpos, sid, snode, text, text2, type, v, _i, _j, _len, _len1, _ref, _ref1;
    _ref = json['snodes'];
    for (k in _ref) {
      v = _ref[k];
      label = v['label'];
      if ((label !== 'x') && (label !== 'X')) {
        sid = k;
        etype = v['etype'];
        rpos = v['rpos'];
        color = v['color'];
        nlist = v['nodes'];
        snode = new SNode(this, sid, etype, rpos, label, color, false);
        this.snodes[sid] = snode;
        for (_i = 0, _len = nlist.length; _i < _len; _i++) {
          nod = nlist[_i];
          nid = nod['id'];
          text = nod['text'];
          text2 = nod['text2'];
          type = nod['type'];
          edge = nod['edge'];
          glow = false;
          if (this.newedges !== void 0) {
            _ref1 = this.newedges;
            for (_j = 0, _len1 = _ref1.length; _j < _len1; _j++) {
              e = _ref1[_j];
              if (e !== '') {
                if (e === edge) {
                  this.changedSNode = snode;
                  glow = true;
                }
              }
            }
          }
          if (type === 'url') {
            node = new Node(nid, text, text2, type, snode, edge, nod['url'], nod['icon'], glow);
          } else {
            node = new Node(nid, text, text2, type, snode, edge, '', '', glow);
          }
          snode.nodes[nid] = node;
        }
        snode.place();
      }
    }
    return this.layout();
  };

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
