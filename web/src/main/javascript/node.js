/**
 * (c) 2012 GraphBrain Ltd. All rigths reserved.
 */

var nodeCount = 0;

// Node
var Node = function(id, text, type, snode) {
    this.id = id;
    this.divid = 'n' + nodeCount++;
    this.text = text;
    this.type = type;
    
    this.rpos = Array(3);
    
    this.subNodes = [];
    this.snode = snode;

    // position in relation to super node
    this.sx = 0;
    this.sy = 0;
}

Node.prototype.calcPos = function() {
    var nodeDiv = $('#' + this.divid)
    var offset = nodeDiv.offset();
    this.rpos[0] = offset.left + this.halfWidth;
    this.rpos[1] = offset.top + this.halfHeight;
    this.rpos[2] = 0;
    this.x0 = this.rpos[0] - this.halfWidth;
    this.y0 = this.rpos[1] - this.halfHeight;
    this.x1 = this.rpos[0] + this.halfWidth;
    this.y1 = this.rpos[1] + this.halfHeight;

    this.sx = this.rpos[0] - this.snode.x - this.snode.halfWidth;
    this.sy = this.rpos[1] - this.snode.y - this.snode.halfHeight;
}

Node.prototype.estimatePos = function() {
    this.rpos[0] = this.snode.rpos[0] + this.sx;
    this.rpos[1] = this.snode.rpos[1] + this.sy;
    this.rpos[2] = this.snode.rpos[2];

    this.x0 = this.rpos[0] - this.halfWidth;
    this.y0 = this.rpos[1] - this.halfHeight;
    this.x1 = this.rpos[0] + this.halfWidth;
    this.y1 = this.rpos[1] + this.halfHeight;
}

Node.prototype.place = function() {
    var node = document.createElement('div');
    node.setAttribute('class', 'node_' + this.snode.depth);
    node.setAttribute('id', this.divid);
    if (this.type == 'text') {
        node.innerHTML = '<a href="/node/' + this.id + '" id="' + this.divid + '">' + this.text + '</a>';
    }
    else if (this.type == 'image') {
        node.innerHTML = '<a href="/node/' + this.id + '" id="' + this.divid + '"><img src="' + this.text + '" width="50px" /></a>';
    }
    var snodeDiv = document.getElementById(this.snode.id);
    snodeDiv.appendChild(node);

    var nodeDiv = $('#' + this.divid)
    var width = nodeDiv.outerWidth();
    var height = nodeDiv.outerHeight();
    // TODO: temporary hack
    if (this.type == 'image') {
        width = 50;
        height = 80;
    }
    
    this.width = width;
    this.height = height;
    this.halfWidth = width / 2;
    this.halfHeight = height / 2;

    /*
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
    */
}