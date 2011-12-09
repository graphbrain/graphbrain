// Super node
var SNode = function(id) {
    this.id = id;
    this.x = 0;
    this.y = 0;
    this.vX = 0;
    this.vY = 0;
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
    this.x0 = this.x - this.halfWidth;
    this.y0 = this.y - this.halfHeight;
    this.x1 = this.x + this.halfWidth;
    this.y1 = this.y + this.halfHeight;
    
    // calc bounding rectangle
    this.rect.v1.x = this.x - this.halfWidth;
    this.rect.v1.y = this.y - this.halfHeight;
    this.rect.v2.x = this.x - this.halfWidth;
    this.rect.v2.y = this.y + this.halfHeight;
    this.rect.v3.x = this.x + this.halfWidth;
    this.rect.v3.y = this.y + this.halfHeight;
    this.rect.v4.x = this.x + this.halfWidth;
    this.rect.v4.y = this.y - this.halfHeight;

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

SNode.prototype.moveTo = function(x, y, redraw) {
    redraw = typeof(redraw) !== 'undefined' ? redraw : true;
    
    this.updatePos(x, y);

    $('div#' + this.id).css('left', (this.x - this.halfWidth) + 'px');
    $('div#' + this.id).css('top', (this.y - this.halfHeight) + 'px');

    // update positions for nodes contained in this super node
    for (var key in this.nodes) {
        if (this.nodes.hasOwnProperty(key))
            this.nodes[key].updatePos();
    }

    if (redraw) {
        g.drawLinks();
    }
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
