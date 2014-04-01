var SNode;

SNode = (function() {

  function SNode(graph, id, etype, relpos, label, color, isRoot) {
    this.graph = graph;
    this.id = id;
    this.etype = etype;
    this.relpos = relpos;
    this.label = label;
    this.color = color;
    this.isRoot = isRoot;
    this.nodes = {};
    this.width = 0;
    this.height = 0;
    this.halfWidth = 0;
    this.halfHeight = 0;
    this.scale = 1;
    this.jqDiv = false;
    this.pos = newv3();
    this.x = 0;
    this.y = 0;
    this.z = 0;
    this.layedOut = false;
  }

  SNode.prototype.initPosAndLayout = function() {
    this.rpos = Array(3);
    this.auxVec = new Array(3);
    this.f = newv3();
    return this.tpos = newv3();
  };

  SNode.prototype.moveTo = function(x, y, z) {
    console.log("moveTo> " + x + " " + y + " " + z);
    var opacity, sc, spread, transformStr;
    this.x = x;
    this.y = y;
    this.z = z;
    this.auxVec[0] = this.x;
    this.auxVec[1] = this.y;
    this.auxVec[2] = this.z;
    console.log("affinMat> " + this.graph.affinMat)
    console.log("rpos0> " + this.rpos);
    m4x4mulv3(this.graph.affinMat, this.auxVec, this.rpos);
    sc = new SphericalCoords(this.graph.negativeStretch, this.graph.mappingPower);
    sc.x = this.rpos[0];
    sc.y = this.rpos[1];
    sc.z = this.rpos[2];
    sc.cartesianToSpherical();
    sc.viewMapping();
    sc.sphericalToCartesian();
    console.log("rpos1> " + this.rpos);
    this.rpos[0] = sc.x;
    this.rpos[1] = sc.y;
    this.rpos[2] = sc.z;
    this.angleX = Math.atan2(sc.y, sc.z);
    this.angleY = Math.atan2(sc.x, sc.z);
    spread = 0.7;
    console.log("rpos2> " + this.rpos);
    this.rpos[0] = this.rpos[0] * this.graph.halfWidth * spread + this.graph.halfWidth;
    this.rpos[1] += this.rpos[1] * this.graph.halfHeight * spread + this.graph.halfHeight;
    this.rpos[2] += this.rpos[2] * Math.min(this.graph.halfWidth, this.graph.halfHeight) * 0.8;
    console.log("rpos3> " + this.rpos);
    x = this.rpos[0];
    y = this.rpos[1];
    z = this.rpos[2] + this.graph.zOffset;
    if (!isNaN(x) && !isNaN(y) && !isNaN(z)) {
      console.log("snode half w/h> " + this.halfWidth + " " + this.halfHeight);
      transformStr = 'translate3d(' + (x - this.halfWidth) + 'px,' + (y - this.halfHeight) + 'px,' + z + 'px)';
      transformStr += ' scale(' + this.scale + ')';
      console.log(transformStr)
      this.jqDiv.css('-webkit-transform', transformStr);
      this.jqDiv.css('-moz-transform', transformStr);
      if (z < 0) {
        opacity = -1 / (z * 0.007);
        return this.jqDiv.css('opacity', opacity);
      } else {
        return this.jqDiv.css('opacity', 1);
      }
    }
  };

  SNode.prototype.applyPos = function() {
    return this.moveTo(this.pos[0], this.pos[1], this.pos[2]);
  };

  SNode.prototype.place = function() {
    var html, key, relText;
    html = '<div id="' + this.id + '" class="snode">';
    if (this.isRoot) {
      html = '<div id="' + this.id + '" class="snodeR">';
    }
    relText = '';
    if (!this.isRoot) {
      relText = this.graph.label(this.label, this.relpos);
    }
    html += '<div class="snodeLabel">' + relText + '</div>';
    html += '<div class="snodeInner">';
    html += '<div class="viewport" /></div></div>';
    $('#graph-view').append(html);
    this.jqDiv = $('#' + this.id);
    for (key in this.nodes) {
      if (this.nodes.hasOwnProperty(key)) {
        this.nodes[key].place();
      }
    }
    if (this.jqDiv.outerHeight() > 250) {
      $('#' + this.id + ' .viewport').slimScroll({
        height: '250px'
      });
      this.jqDiv.hover(scrollOn, scrollOff);
    }
    this.width = this.jqDiv.outerWidth();
    this.height = this.jqDiv.outerHeight();
    this.halfWidth = this.width / 2;
    this.halfHeight = this.height / 2;
    if (this.initialWidth < 0) {
      this.initialWidth = this.width;
    }
    if (!this.isRoot) {
      return this.setColor(this.color);
    }
  };

  SNode.prototype.setColor = function(color) {
    $('#' + this.id).css('border-color', color);
    return $('#' + this.id + ' .snodeLabel').css('background', color);
  };

  SNode.prototype.toString = function() {
    var key;
    for (key in this.nodes) {
      if (this.nodes.hasOwnProperty(key)) {
        return '{' + this.nodes[key].text + ', ...}';
      }
    }
  };

  return SNode;

})();
