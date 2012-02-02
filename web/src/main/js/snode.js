/**
 * (c) 2012 GraphBrain Ltd. All rigths reserved.
 */

// Super node
var SNode = function(id) {
    this.id = id;
    
    // position before rotation
    this.x = 0;
    this.y = 0;
    this.z = 0;
    // position after rotation
    this.rpos = Array(3);

    // auxiliary vector for rotation calcs
    this.auxVec = new Array(3);

    this.nodes = {};
    this.subNodes = [];
    this.parent = 'unknown';
    this.links = [];
    this.weight = 0;

    // add common VisualObj capabilities
    makeVisualObj(this);
}

SNode.prototype.updatePos = function(x, y) {
    this.x = x;
    this.y = y;
    this.z = 0;

    // rotation
    this.auxVec[0] = this.x - g.halfWidth;
    this.auxVec[1] = this.y - g.halfHeight;
    this.auxVec[2] = 0;

    m4x4mulv3(g.affinMat, this.auxVec, this.rpos);
    
    this.rpos[0] += g.halfWidth;
    this.rpos[1] += g.halfHeight;

    // limits used to place links
    this.x0 = this.rpos[0] - this.halfWidth;
    this.y0 = this.rpos[1] - this.halfHeight;
    this.x1 = this.rpos[0] + this.halfWidth;
    this.y1 = this.rpos[1] + this.halfHeight;
    
    // calc bounding rectangle
    this.rect.v1.x = this.rpos[0] - this.halfWidth;
    this.rect.v1.y = this.rpos[1] - this.halfHeight;
    this.rect.v2.x = this.rpos[0] - this.halfWidth;
    this.rect.v2.y = this.rpos[1] + this.halfHeight;
    this.rect.v3.x = this.rpos[0] + this.halfWidth;
    this.rect.v3.y = this.rpos[1] + this.halfHeight;
    this.rect.v4.x = this.rpos[0] + this.halfWidth;
    this.rect.v4.y = this.rpos[1] - this.halfHeight;

    // update position of contained nodes
    for (var key in this.nodes) {
        if (this.nodes.hasOwnProperty(key))
            this.nodes[key].estimatePos();
    }

    // update position of connected links
    for (var i = 0; i < this.links.length; i++) {
        var link = this.links[i];
        link.updatePos();
    }
}

SNode.prototype.moveTo = function(x, y) {
    this.updatePos(x, y);

    var transformStr = 'translate3d(' + (this.rpos[0] - this.halfWidth)
        + 'px,' + (this.rpos[1] - this.halfHeight) + 'px,' + this.rpos[2] + 'px)';
    $('div#' + this.id).css('-webkit-transform', transformStr);
}

SNode.prototype.place = function() {
    var snode = document.createElement('div');
    
    var nodesCount = 0;
    for (var key in this.nodes) {
        if (this.nodes.hasOwnProperty(key))
            nodesCount++;
    }
    if (nodesCount > 1) {
        snode.setAttribute('class', 'snode_' + this.depth);
    }
    else {
        snode.setAttribute('class', 'snode1_' + this.depth);
    }
    snode.setAttribute('id', this.id);
    
    var nodesDiv = document.getElementById("nodesDiv");
    nodesDiv.appendChild(snode);

    // place nodes contained in this super node
    for (var key in this.nodes) {
        if (this.nodes.hasOwnProperty(key))
            this.nodes[key].place();
    }

    var width = $('div#' + this.id).outerWidth();
    var height = $('div#' + this.id).outerHeight();
    
    this.width = width;
    this.height = height;
    this.halfWidth = width / 2;
    this.halfHeight = height / 2;
    this.moveTo(this.x, this.y);

    // calc relative positions of nodes contained in this super node
    for (var key in this.nodes) {
        if (this.nodes.hasOwnProperty(key))
            this.nodes[key].calcPos();
    }

    var nodeObj = this;

    $("div#" + this.id).bind("mousedown", function(e) {
        if (uiMode === 'drag') {
            draggedNode = nodeObj;
            return false;
        }
        else {
            newLink = new Link(0, nodeObj, false, '...');
            newLink.tx = e.pageX;
            newLink.ty = e.pageY;
            return false;
        }
    });

    $("div#" + this.id).bind("click", function(e) {
        if (dragging) {
            dragging = false;
            return false;
        }
        else {
            return true;
        }
    });

    $("div#" + this.id).hover(
    function(e) {
        if (newLink) {
            newLink.targ = nodeObj;
        }
    },
    function(e) {});
}

SNode.prototype.toString = function() {
    var key;
    for (key in this.nodes) {
        if (this.nodes.hasOwnProperty(key))
            return '{' + this.nodes[key].text + ', ...}';
    }
}