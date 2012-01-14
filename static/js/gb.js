// extending Object to return size (useful for dictionaries)
Object.prototype.size = function () {
    var len = this.length ? --this.length : -1;
    for (var k in this)
        len++;
    return len;
}
// Geom

var rotateAndTranslate = function(point, angle, tx, ty) {
    var x = point[0];
    var y = point[1];

    var rx = Math.cos(angle) * x - Math.sin(angle) * y;
    var ry = Math.sin(angle) * x + Math.cos(angle) * y;

    x = rx + tx;
    y = ry + ty;

    point[0] = x;
    point[1] = y;
}


var dotProduct = function(p0, p1) {
    return (p0[0] * p1[0]) + (p0[1] * p1[1]);
}


var pointInTriangle = function(A, B, C, P) {
    var v0 = [0, 0];
    var v1 = [0, 0];
    var v2 = [0, 0];
    
    v0[0] = C[0] - A[0];
    v0[1] = C[1] - A[1];
    v1[0] = B[0] - A[0];
    v1[1] = B[1] - A[1];
    v2[0] = P[0] - A[0];
    v2[1] = P[1] - A[1];

    var dot00 = dotProduct(v0, v0);
    var dot01 = dotProduct(v0, v1);
    var dot02 = dotProduct(v0, v2);
    var dot11 = dotProduct(v1, v1);
    var dot12 = dotProduct(v1, v2);

    var invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
    var u = (dot11 * dot02 - dot01 * dot12) * invDenom;
    var v = (dot00 * dot12 - dot01 * dot02) * invDenom;

    return (u > 0) && (v > 0) && (u + v < 1);
}


/*
Return the intersection point between the line segment defined by (x1, y1) and (x2, y2)
and a rectangle defined by (rleft, rtop, rright, rbottom)

(x1, y1) is assumed to be inside the rectangle and (x2, y2) outside 
*/
var interRect = function(x1, y1, x2, y2, rleft, rtop, rright, rbottom) {
    var t, tx, ty, edge;
    
    var dx = x2 - x1;
    var dy = y2 - y1;
    
    if ((dx == 0) && (dy == 0)) {
        return 0;
    }

    // Let x = x1 + dx * t  and calculate t at the intersection point with a vertical border.
    if (dx != 0) {
        var edge;
        if (dx > 0) {
            edge = rright;
        }
        else {
            edge = rleft;
        }
        tx = (edge - x1) / dx;
    }

    // Let y = y1 + dy * t and calculate t for the vertical border.
    if (dy != 0) {
        var edge;
        if (dy > 0) {
            edge = rbottom;
        }
        else {
            edge = rtop;
        }
        ty = (edge - y1) / dy;
    }

    // Then take the shorter one.
    if (dx == 0) {
        t = ty;
    }
    else if (dy == 0) {
        t = tx;
    }
    else {
        if (tx < ty) {
            t = tx;
        }
        else {
            t = ty;
        }
    }

    // Calculate the coordinates of the intersection point.
    var ix = x1 + dx * t;
    var iy = y1 + dy * t;
    return [ix, iy];
}


var rectsOverlap = function(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2) {
    if (r1_x1 < r2_x2 && r1_x2 > r2_x1 && r1_y1 < r2_y2 && r1_y2 > r2_y1) {
        return true;
    }
    return false;
}


var sepAxisSide = function(a1, a2, point) {
    //var rx = -a1.y;
    //var ry = a1.x;

    //var dp = rx * (point.x - a2.x) + ry * (point.y - a2.y);

    var dp = ((a2.x - a1.x) * (point.y - a1.y)) - ((a2.y - a1.y) * (point.x - a1.x));

    if (dp < 0)
        return -1;
    else
        return 1;
}


var sepAxis = function(a1, a2, point, rect) {
    var sign1 = sepAxisSide(a1, a2, point);
    var sign2 = sepAxisSide(a1, a2, rect.v1);

    if (sign1 == sign2)
        return false;

    if (sign2 != sepAxisSide(a1, a2, rect.v2))
        return false;
    if (sign2 != sepAxisSide(a1, a2, rect.v3))
        return false;
    if (sign2 != sepAxisSide(a1, a2, rect.v4))
        return false;
    
    return true;
}


var rotRectsOverlap = function(rect1, rect2) {
    if (sepAxis(rect1.v1, rect1.v2, rect1.v3, rect2))
        return false;
    if (sepAxis(rect1.v2, rect1.v3, rect1.v1, rect2))
        return false;
    if (sepAxis(rect1.v3, rect1.v4, rect1.v1, rect2))
        return false;
    if (sepAxis(rect1.v4, rect1.v1, rect1.v2, rect2))
        return false;
    if (sepAxis(rect2.v1, rect2.v2, rect2.v3, rect1))
        return false;
    if (sepAxis(rect2.v2, rect2.v3, rect2.v1, rect1))
        return false;
    if (sepAxis(rect2.v3, rect2.v4, rect2.v1, rect1))
        return false;
    if (sepAxis(rect2.v4, rect2.v1, rect2.v2, rect1))
        return false;
    
    return true;
}


var rectsDist2 = function(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2) {
    if (rectsOverlap(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2)) {
        return 0;
    }

    var c1_x = r1_x1 + ((r1_x2 - r1_x1) / 2);
    var c1_y = r1_y1 + ((r1_y2 - r1_y1) / 2);
    var c2_x = r2_x1 + ((r2_x2 - r2_x1) / 2);
    var c2_y = r2_y1 + ((r2_y2 - r2_y1) / 2);

    var p1 = interRect(c1_x, c1_y, c2_x, c2_y, r1_x1, r1_y1, r1_x2, r1_y2);
    var p2 = interRect(c2_x, c2_y, c1_x, c1_y, r2_x1, r2_y1, r2_x2, r2_y2);

    var deltaX = p1[0] - p2[0];
    var deltaY = p1[1] - p2[1];

    var dist = (deltaX * deltaX) + (deltaY * deltaY);

    return dist;
}


var rectsDist = function(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2) {
    var dist = rectsDist2(r1_x1, r1_y1, r1_x2, r1_y2, r2_x1, r2_y1, r2_x2, r2_y2);
    dist = Math.sqrt(dist);
    return dist;
}


var lineSegsOverlap = function(x1, y1, x2, y2, x3, y3, x4, y4) {
    var denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);

    var ua = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
    var ub = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);

    // lines are parallel
    if (denom == 0) {
        // coincident?
        if ((ua == 0) && (ub == 0))
            return true;
        else
            return false;
    }

    ua /= denom;
    ub /= denom;

    if ((ua >= 0) && (ua <= 1) && (ub >= 0) && (ub <= 1))
        return true;
    else
        return false;
}


var lineRectOverlap = function(x1, y1, x2, y2, rect) {
    if (lineSegsOverlap(x1, y1, x2, y2, rect.v1.x, rect.v1.y, rect.v2.x, rect.v2.y)) return true;
    if (lineSegsOverlap(x1, y1, x2, y2, rect.v2.x, rect.v2.y, rect.v3.x, rect.v3.y)) return true;
    if (lineSegsOverlap(x1, y1, x2, y2, rect.v3.x, rect.v3.y, rect.v4.x, rect.v4.y)) return true;
    if (lineSegsOverlap(x1, y1, x2, y2, rect.v4.x, rect.v4.y, rect.v1.x, rect.v1.y)) return true;
    return false;
}
// VisualObj
var makeVisualObj = function(that) {
	that.rect = [];
    that.rect.v1 = [];
    that.rect.v2 = [];
    that.rect.v3 = [];
    that.rect.v4 = [];

    that.rect.v1.x = 0;
    that.rect.v1.y = 0;
    that.rect.v2.x = 0;
    that.rect.v2.y = 0;
    that.rect.v3.x = 0;
    that.rect.v3.y = 0;
    that.rect.v4.x = 0;
    that.rect.v4.y = 0;

	that.overlaps = function(obj) {
		return rotRectsOverlap(that.rect, obj.rect);
	}
}
var nodeCount = 0;

// Node
var Node = function(id, text, type, snode) {
    this.id = id;
    this.divid = 'n' + nodeCount++;
    this.text = text;
    this.type = type;
    this.x = 0;
    this.y = 0;
    this.vX = 0;
    this.vY = 0;
    this.subNodes = [];
    this.snode = snode;

    // position in relation to super node
    this.sx = 0;
    this.sy = 0;
}

Node.prototype.updatePos = function() {
    var nodeDiv = $('#' + this.divid)
    var offset = nodeDiv.offset();
    this.x = offset.left + this.halfWidth;
    this.y = offset.top + this.halfHeight;
    this.x0 = this.x - this.halfWidth;
    this.y0 = this.y - this.halfHeight;
    this.x1 = this.x + this.halfWidth;
    this.y1 = this.y + this.halfHeight;

    this.sx = this.x - this.snode.x;
    this.sy = this.y - this.snode.y;
}

Node.prototype.estimatePos = function() {
    this.x = this.snode.x + this.sx;
    this.y = this.snode.y + this.sy;

    this.x0 = this.x - this.halfWidth;
    this.y0 = this.y - this.halfHeight;
    this.x1 = this.x + this.halfWidth;
    this.y1 = this.y + this.halfHeight;
    //console.log('nx: ' + this.x + '; ny: ' + this.y);
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
   
    this.updatePos();

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

    var a = g.viewAngleX;
    var tx = this.x - g.halfWidth;
    var ty = this.y - g.halfHeight;
    var rx = Math.round(tx * Math.cos(a));
    var ry = Math.round(ty);
    var rz = Math.round(-tx * Math.sin(a));
    rx += g.halfWidth;
    ry += g.halfHeight;

    var transformStr = 'translate3d(' + (rx - this.halfWidth) + 'px,' + (ry - this.halfHeight) + 'px,' + rz + 'px)';
    $('div#' + this.id).css('-webkit-transform', transformStr);

    // update positions for nodes contained in this super node
    for (var key in this.nodes) {
        if (this.nodes.hasOwnProperty(key))
            this.nodes[key].updatePos();
    }

    /*if (redraw) {
        g.drawLinks();
    }*/
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

// Link
var Link = function(id, orig, sorig, targ, starg, label) {
    this.id = id;
    this.orig = orig;
    this.sorig = sorig;
    this.targ = targ;
    this.starg = starg;
    this.label = label;
    this.ox = 0;
    this.oy = 0;
    this.tx = 0;
    this.ty = 0;

    this.len = 0;

    // calc width & height
    context.font = "10pt Sans-Serif";
    var dim = context.measureText(this.label);
    this.width = dim.width + 6;
    this.height = 18;
    this.halfWidth = this.width / 2;
    this.halfHeight = this.height / 2;

    // add common VisualObj capabilities
    makeVisualObj(this);
}

Link.prototype.updatePos = function() {
    var orig = false;
    var targ = false;
    var origSuper = false;
    var targSuper = false;

    if (this.orig) {
        orig = this.orig;
    }
    else if (this.sorig) {
        orig = this.sorig;
        this.origSuper = true;
    }
    
    if (this.targ) {
        targ = this.targ;
    }
    else if (this.starg) {
        targ = this.starg;
        this.targSuper = true;
    }

    var x0 = orig.x;
    var y0 = orig.y;
    var x1 = targ.x;
    var y1 = targ.y;

    p0 = interRect(x0, y0, x1, y1, orig.x0, orig.y0, orig.x1, orig.y1);
    p1 = interRect(x1, y1, x0, y0, targ.x0, targ.y0, targ.x1, targ.y1);

    this.x0 = p0[0];
    this.y0 = p0[1];
    this.x1 = p1[0];
    this.y1 = p1[1];

    // calc length
    var dx = this.x1 - this.x0;
    var dy = this.y1 - this.y0;
    this.len = (dx * dx) + (dy * dy);
    this.len = Math.sqrt(this.len);

    //console.log("x0: " + this.x0 + "; y0: " + this.y0 + "; x1: " + this.x1 + "; y1: " + this.y1 + "; len: " + this.len);

    // calc center
    this.cx = this.x0 + ((this.x1 - this.x0) / 2)
    this.cy = this.y0 + ((this.y1 - this.y0) / 2)

    var slope = (this.y1 - this.y0) / (this.x1 - this.x0);
    this.angle = Math.atan(slope);

    var p = [[0, 0], [0, 0], [0, 0], [0, 0], [0, 0]];

    if ((this.x0 < this.x1) || ((this.x0 == this.x1) && (this.y0 < this.y1))) {
        p[0][0] = -this.halfWidth;     p[0][1] = -this.halfHeight;
        p[1][0] = -this.halfWidth;     p[1][1] = this.halfHeight;
        p[2][0] = this.halfWidth;      p[2][1] = this.halfHeight;
        p[3][0] = this.halfWidth + 6;  p[3][1] = 0;
        p[4][0] = this.halfWidth;      p[4][1] = -this.halfHeight;
    }
    else {
        p[0][0] = -this.halfWidth;     p[0][1] = -this.halfHeight;
        p[1][0] = -this.halfWidth - 6; p[1][1] = 0;
        p[2][0] = -this.halfWidth;     p[2][1] = this.halfHeight;
        p[3][0] = this.halfWidth;      p[3][1] = this.halfHeight;
        p[4][0] = this.halfWidth;      p[4][1] = -this.halfHeight;
    }

    for (i = 0; i < 5; i++) {
        rotateAndTranslate(p[i], this.angle, this.cx, this.cy);
    }

    this.points = p;

    // calc bounding rectangle
    if ((this.x0 < this.x1) || ((this.x0 == this.x1) && (this.y0 < this.y1))) {
        this.rect.v1.x = p[0][0];
        this.rect.v1.y = p[0][1];
        this.rect.v2.x = p[1][0];
        this.rect.v2.y = p[1][1];
        this.rect.v3.x = p[2][0];
        this.rect.v3.y = p[2][1];
        this.rect.v4.x = p[4][0];
        this.rect.v4.y = p[4][1];
    }
    else {
        this.rect.v1.x = p[0][0];
        this.rect.v1.y = p[0][1];
        this.rect.v2.x = p[2][0];
        this.rect.v2.y = p[2][1];
        this.rect.v3.x = p[3][0];
        this.rect.v3.y = p[3][1];
        this.rect.v4.x = p[4][0];
        this.rect.v4.y = p[4][1];   
    }
}

Link.prototype.draw = function() {
    this.updatePos();

    // set link color according to label text
    // TODO: this should be server side 
    var color = '#FFD326';
    var textcolor = '#000'

    if (~this.label.indexOf('direct'))
        color = '#BEE512';
    else if (~this.label.indexOf('char'))
        color = '#DFFD59';
    else if (~this.label.indexOf('play'))
        color = '#FFFC26';
    else if (~this.label.indexOf('is')) {
        color = '#ED9107';
        textcolor = '#FFF'
    }

    // draw line
    context.strokeStyle = color;
    context.fillStyle = color;
    context.lineWidth = 0.7;

    context.beginPath();
    context.moveTo(this.x0, this.y0);
    context.lineTo(this.x1, this.y1);
    context.stroke();

    // draw circles at both ends of line
    if (this.origSuper) {
        context.fillStyle = '#505050';
    }
    else {
        context.fillStyle = color;   
    }
    context.beginPath();
    var radius = 4;
    context.arc(this.x0, this.y0, radius, 0, 2 * Math.PI, false);
    context.fill();
    if (this.targSuper) {
        context.fillStyle = '#505050';
    }
    else {
        context.fillStyle = color;   
    }
    context.beginPath();
    context.arc(this.x1, this.y1, radius, 0, 2 * Math.PI, false);
    context.fill();
    context.fillStyle = color;

    // draw label area
    context.beginPath();
    context.moveTo(this.points[0][0], this.points[0][1]);
    for (i = 1; i < 5; i++) {
        context.lineTo(this.points[i][0], this.points[i][1]);
    }
    
    context.closePath();
    context.fill();

    // draw label text
    context.save();
    context.translate(this.cx, this.cy);
    context.rotate(this.angle);
    context.font = "10pt Sans-Serif";
    context.fillStyle = textcolor;
    context.textAlign = "center";
    context.textBaseline = "middle";
    context.fillText(this.label, 0, 0);
    context.restore();
}

Link.prototype.pointInLabel = function(p) {
    return (pointInTriangle(this.points[0], this.points[1], this.points[2], p)
        || pointInTriangle(this.points[2], this.points[3], this.points[4], p)
        || pointInTriangle(this.points[0], this.points[2], this.points[4], p));
}


Link.prototype.intersectsLink = function(link2) {
    return lineSegsOverlap(this.x0, this.y0, this.x1, this.y1, link2.x0, link2.y0, link2.x1, link2.y1);
}


Link.prototype.intersectsSNode = function(snode) {
    return lineRectOverlap(this.x0, this.y0, this.x1, this.y1, snode.rect);
}
// GraphPos is an auxliary class used to layout snodes
var GraphPos = function(snode, width, height) {
    this.angDivs = 12;
    this.radDivs = 10;
    this.ang2 = Math.PI * 0.5;

    this.snode = snode;
    this.halfWidth = width / 2;
    this.halfHeight = height / 2;

    this.done = false;
    this.angStep = 0;
    this.radStep = 1;
    this.x = this.halfWidth;
    this.y = this.halfHeight;

    if (this.snode.depth > 1) {
        var deltaX = this.snode.parent.x - this.halfWidth;
        var deltaY = this.snode.parent.y - this.halfHeight;
        this.baseAngle = Math.atan2(deltaY, deltaX);
        this.minRadius = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        this.maxRadius = Math.sqrt((this.halfWidth * this.halfWidth) + (this.halfHeight * this.halfHeight));
    }

    this.next();
}

GraphPos.prototype.next = function() {
    if (this.snode.depth == 1)
        this.next1();
    else
        this.next2();
}

GraphPos.prototype.next1 = function() {
    if (this.angStep >= this.angDivs) {
        this.radStep++;
        this.angStep = 0;
    }

    if (this.radStep > this.radDivs) {
        this.done = true;
        return;
    }

    var angle = Math.PI * 2 * (this.angStep / this.angDivs);
    var a = this.halfWidth * (this.radStep / this.radDivs);
    var b = this.halfHeight * (this.radStep / this.radDivs);

    this.x = this.halfWidth + (a * Math.cos(angle));
    this.y = this.halfHeight + (b * Math.sin(angle));

    this.angStep++;
}

GraphPos.prototype.next2 = function() {
    if (this.angStep >= this.angDivs) {
        this.radStep++;
        this.angStep = 0;
    }

    if (this.radStep >= this.radDivs) {
        this.done = true;
        return;
    }

    var angle = (this.ang2 * (this.angStep / this.angDivs)) - (this.ang2 / 2);
    angle += this.baseAngle;

    var r = this.radStep / this.radDivs;
    r *= this.maxRadius - this.minRadius;
    r += this.minRadius;

    this.x = this.halfWidth + (r * Math.cos(angle));
    this.y = this.halfHeight + (r * Math.sin(angle));

    this.angStep++;
}


// Graph
var Graph = function() {
    this.snodes = {}
    this.nodes = {};
    this.links = [];
    this.newNode = false;
    this.newNodeActive = false;
    this.viewAngleX = 0;
    this.viewAngleY = 0;
}

Graph.prototype.drawLinks = function() {
    context.clearRect(0, 0, context.canvas.width, context.canvas.height);
    var i;
    for (i = 0; i < this.links.length; i++) {
        this.links[i].draw();
    }

    if (newLink) {
        newLink.draw();
    }
}

Graph.prototype.placeNodes = function() {
    for (var key in this.snodes) {
        if (this.snodes.hasOwnProperty(key))
            this.snodes[key].place();
    }
}

Graph.prototype.updateView = function() {
    for (var key in this.snodes) {
        if (this.snodes.hasOwnProperty(key)) {
            var sn = this.snodes[key];
            sn.moveTo(sn.x, sn.y);
        }
    }
}

Graph.prototype.labelAtPoint = function(x, y) {
    var p = [x, y];
    for (var i = this.links.length - 1; i >= 0; i--) {
        if (this.links[i].pointInLabel(p)) {
            return this.links[i];
        }
    }

    return -1;
}

Graph.prototype.genSNodeKeys = function() {
    this.snodeKeys = []
    for (var key in this.snodes) {
        this.snodeKeys.push(key);
    }
}

Graph.prototype.layoutSNode = function(snode, fixedSNodes, width, height) {
    var iters = 100;

    snode.fixed = true;

    var bestPenalty = 99999999;
    var bestX, bestY;
    
    var gp = new GraphPos(snode, width, height);

    while (!gp.done) {
        var penalty = 0;

        var x = gp.x;
        var y = gp.y;
        snode.updatePos(x, y);

        for (var j = 0; j < fixedSNodes.length; j++) {
            var snode2 = fixedSNodes[j];

            // node - node overlap penalty
            if (snode.overlaps(snode2)) {
                penalty += 1000000;
            }

            // label - node overlap penalty
            for (var k = 0; k < snode.links.length; k++) {
                var link = snode.links[k];
                if (link.overlaps(snode2)) {
                    penalty += 10000;
                }
            }

            // link-node intersection penalty
            /*for (var k = 0; k < snode.links.length; k++) {
                var link = snode.links[k];
                if (link.sorig.fixed && link.starg.fixed)
                    if (link.intersectsSNode(snode2))
                        penalty += 10000;
            }*/
        }

        // node-label overlap penalty
        for (var k = 0; k < this.links.length; k++) {
            var link = this.links[k];
            if (link.sorig.fixed && link.starg.fixed)
                if (snode.overlaps(link))
                    penalty += 10000;
        }

        // link-link intersection penalty
        for (var k = 0; k < this.links.length; k++) {
            var link = this.links[k];
            if (link.sorig.fixed && link.starg.fixed) {
                for (var l = 0; l < snode.links.length; l++) {
                    var slink = snode.links[l];
                    if (slink.sorig.fixed && slink.starg.fixed) {
                        if (link.intersectsLink(slink)) {
                            penalty += 10000;
                        }
                    }
                }
            }
        }

        // link length penalty
        for (var k = 0; k < snode.links.length; k++) {
            var link = snode.links[k];
            if (link.sorig.fixed && link.starg.fixed)
                penalty += link.len;
        }

        //console.log("p: " + penalty + "; count:" + snode.links.length);

        if (penalty < bestPenalty) {
            bestPenalty = penalty;
            bestX = x;
            bestY = y;
        }

        gp.next();
    }

    //console.log("best: " + bestPenalty);

    snode.moveTo(bestX, bestY);
}

Graph.prototype.nextByWeight = function(depth) {
    var bestWeight = -1;
    var bestSNode = false;
    for (var key in this.snodes) {
        if (this.snodes.hasOwnProperty(key)) {
            var snode = this.snodes[key];
            if ((!snode.fixed) && (snode.depth == depth)) {
                if (snode.weight > bestWeight) {
                    bestWeight = snode.weight;
                    bestSNode = snode;
                }
            }
        }
    }

    return bestSNode;
}

Graph.prototype.layout = function(width, height) {
    this.halfWidth = width / 2;
    this.halfHeight = height / 2;

    // set all super nodes non-fixed
    for (var key in this.snodes) {
        if (this.snodes.hasOwnProperty(key)) {
            var snode = this.snodes[key];
            snode.fixed = false;
        }
    }

    // layout root node
    var fixedSNodes = [g.root];
    g.root.moveTo(width / 2, height / 2);
    g.root.fixed = true;
    
    var snodeCount = this.snodes.size();

    // special cases
    if (snodeCount > 1) {
        var snode = this.nextByWeight(1);
        var x = width / 2;
        x -= g.root.width / 2;
        x -= snode.width / 2;
        x -= 100;
        var y = height / 2;
        snode.moveTo(x, y);
        snode.fixed = true;
        fixedSNodes.push(snode);
    }
    if (snodeCount > 2) {
        var snode = this.nextByWeight(1);
        if (snode) {
            var x = width / 2;
            x += g.root.width / 2;
            x += snode.width / 2;
            x += 100;
            var y = height / 2;
            snode.moveTo(x, y);
            snode.fixed = true;
            fixedSNodes.push(snode);
        }
    }
    if (snodeCount > 3) {
        var snode = this.nextByWeight(1);
        if (snode) {
            var x = width / 2;
            var y = height / 2;
            y -= g.root.height / 2;
            y -= snode.height / 2;
            y -= 100;
            snode.moveTo(x, y);
            snode.fixed = true;
            fixedSNodes.push(snode);
        }
    }
    if (snodeCount > 4) {
        var snode = this.nextByWeight(1);
        if (snode) {
            var x = width / 2;
            var y = height / 2;
            y += g.root.height / 2;
            y += snode.height / 2;
            y += 100;
            snode.moveTo(x, y);
            snode.fixed = true;
            fixedSNodes.push(snode);
        }
    }

    // layout tier 1 nodes
    for (var key in this.snodes) {
        if (this.snodes.hasOwnProperty(key)) {
            var snode = this.snodes[key];
            if ((snode.depth == 1) && (!snode.fixed)) {
                this.layoutSNode(snode, fixedSNodes, width, height);
                fixedSNodes.push(snode);
            }
        }
    }

    // layout tier 2 nodes
    for (var key in this.snodes) {
        if (this.snodes.hasOwnProperty(key)) {
            var snode = this.snodes[key];
            if (snode.depth == 2) {
                this.layoutSNode(snode, fixedSNodes, width, height);
                fixedSNodes.push(snode);
            }
        }
    }
}
// UI modes
var dragMode = function(event) {
    uiMode = 'drag';
    $("#dragModeButton").addClass("selModeButton");
    $("#addModeButton").removeClass("selModeButton");
}

var addMode = function(event) {
    uiMode = 'add';
    $("#dragModeButton").removeClass("selModeButton");
    $("#addModeButton").addClass("selModeButton");
    $("#tip").html('Try clicking & dragging!');
    $("#tip").fadeIn("slow", function(){tipVisible = true;});    
}
// Entry point functions & global variables
var g;
var context;
var uiMode;
var draggedNode;
var dragging;
var newLink;
var tipVisible;

var initInterface = function() {
    if (error != '') {
        $("#tip").html('<div class="error">' + error + '</div>');
        $("#tip").fadeIn("slow", function(){tipVisible = true;});       
    }

    newLink = false;
    draggedNode = false;
    dragging = false;
    curTargNode = false;
    tipVisible = false;

    $("#nodesDiv").bind("mouseup", (function(e) {
        g.viewAngleX += 0.1;
        g.updateView();
        
        if (uiMode === 'drag') {
            draggedNode = false;
        }
        else { 
            if ((newLink.orig) || (newLink.targ)) {
                if (tipVisible) {
                    $(".tip").fadeOut("slow", function(){tipVisible = false;});
                }
                $('#overlay').fadeIn(80, (function(e) {
                    $('#box').css({visibility:'visible'});
                    if (newLink.orig) {
                        $('#dNode1').html(newLink.orig.text);
                        $('#dNode1_id').val(newLink.orig.id);
                        $('#dNode1').css({display:'block'});
                        $('#dNode1In').css({display:'none'});
                    }
                    else {
                        $('#dNode1').css({display:'none'});
                        $('#dNode1In').css({display:'block'});
                        $('#dNode1_id').val('none');
                    }
                    if (newLink.targ) {
                        $('#dNode2').html(newLink.targ.text);
                        $('#dNode2_id').val(newLink.targ.id);
                        $('#dNode2').css({display:'block'});
                        $('#dNode2In').css({display:'none'});
                    }
                    else {
                        $('#dNode2').css({display:'none'});
                        $('#dNode2In').css({display:'block'});
                        $('#dNode2_id').val('none');
                    }
                    newLink.targ = false;
                }));
            }
            else {
                newLink = false;
                g.drawLinks();
            }
        }
    }));

    $("#nodesDiv").bind("mousedown", function(e) {
        if (uiMode === 'drag') {
            return false;
        }
        else {
            newLink = new Link(0, false, false, '...');
            newLink.ox = e.pageX;
            newLink.oy = e.pageY;
            newLink.tx = e.pageX;
            newLink.ty = e.pageY;
            return false;
        }
    });

    $("#nodesDiv").bind("mousemove", (function(e) {
        if (uiMode === 'drag') {
            if (draggedNode) {
                draggedNode.moveTo(e.pageX, e.pageY);
                dragging = true;
            }
        }
        else {
            if (newLink) {
                newLink.tx = e.pageX;
                newLink.ty = e.pageY;
                g.drawLinks();
                return false;
            }
        }
    }));

    $('#boxclose').click(function(){
        $('#overlay').fadeOut(80, (function(e) {
            $('#box').css({visibility:'hidden'});
            newLink = false;
            g.drawLinks();
        }));
    });

    dragMode();
    $("#dragModeButton").bind("click", dragMode);
    $("#addModeButton").bind("click", addMode);


    $("#nodesDiv").bind("click", (function(event) {
        l = g.labelAtPoint(event.pageX, event.pageY);
        if (l != -1) {
            if (confirm('Do you want to delete connection:\n"' + l.orig.text + ' ' + l.type + ' ' + l.targ.text + '"?')) {
                document.forms["delinkForm"].elements["link_orig"].value = l.orig.id;
                document.forms["delinkForm"].elements["link_targ"].value = l.targ.id;
                document.forms["delinkForm"].elements["link_type"].value = l.type;
                $("#delinkForm").submit();
            }
        }
    }));    
}

var initGraph = function() {
    var elem = document.getElementById('graphCanvas');
    context = elem.getContext('2d');

    g = new Graph();

    context.translate(0.5, 0.5)

    // process super nodes and associated nodes
    var i, j;
    for (i = 0; i < snodes.length; i++) {
        var sn = snodes[i];
        var sid = sn['id'];
        var nlist = sn['nodes'];
        
        var snode = new SNode(sid)

        for (j = 0; j < nlist.length; j++) {
            var nid = nlist[j];
            var nod = nodes[nid];
            var text = nod['text'];
            var type = nod['type'];
            var parentID = nod['parent'];
            var node = new Node(nid, text, type, snode);
            snode.nodes[nid] = node;
            g.nodes[nid] = node;

            if ((snode.parent == 'unknown') || (parentID == '')) {
                snode.parent = parentID;
            }
        }
        g.snodes[sid] = snode;
    }   

    // assign root, parents and subNodes
    for (i = 0; i < snodes.length; i++) {
        var sid = snodes[i]['id'];
        var snode = g.snodes[sid];
        var parentID = snode.parent;
        if (parentID == '') {
            g.root = snode;
            snode.parent = false;
        }
        else {
            snode.parent = g.nodes[parentID].snode;
            g.nodes[parentID].snode.subNodes[g.nodes[parentID].snode.subNodes.length] = snode;
        }
    }

    // assign depth and weight
    for (var key in g.snodes) {
        if (g.snodes.hasOwnProperty(key)) {
            var snode = g.snodes[key];
            snode.weight = snode.nodes.size();
            if (snode.parent == '') {
                snode.depth = 0;
            }
            else if (snode.parent == g.root) {
                snode.depth = 1;

                for (var i = 0; i < snode.subNodes.length; i++) {
                    var subNode = snode.subNodes[i];              
                    snode.weight += subNode.nodes.size(); 
                }
            }
            else {
                snode.depth = 2;
            }
        }
    }

    g.genSNodeKeys();

    // process links
    for (i = 0; i < links.length; i++) {
        var l = links[i];
        var orig = '';
        var targ = '';
        var sorig = '';
        var starg = '';
        if ('orig' in l) {
            orig  = g.nodes[l['orig']];
            sorig = orig.snode;
        }
        else {
            orig = false;
            sorig = g.snodes[l['sorig']];
        }
        if ('targ' in l) {
            targ  = g.nodes[l['targ']];
            starg = targ.snode;
        }
        else {
            targ = false;
            starg  = g.snodes[l['starg']]
        }
        var link = new Link(l['id'], orig, sorig, targ, starg, l['relation']);
        g.links.push(link);
        sorig.links.push(link);
        starg.links.push(link);
    }
    
    var halfWidth = window.innerWidth / 2;
    var halfHeight = window.innerHeight / 2;

    g.placeNodes();
    g.layout(window.innerWidth, window.innerHeight);

    context.canvas.width  = window.innerWidth;
    context.canvas.height = window.innerHeight;

    g.drawLinks();
}

$(function() {
    initInterface();
    initGraph();
});
