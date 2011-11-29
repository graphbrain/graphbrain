// Graph
var Graph = function(context) {
    this.context = context;
    this.snodes = {}
    this.nodes = {};
    this.links = [];
    this.newNode = false;
    this.newNodeActive = false;
}

Graph.prototype.drawLinks = function() {
    this.context.clearRect(0, 0, this.context.canvas.width, this.context.canvas.height);
    var i;
    for (i = 0; i < this.links.length; i++) {
        this.links[i].draw(this.context);
    }

    if (newLink) {
        newLink.draw(this.context);
    }
}

Graph.prototype.placeNodes = function() {
    for (var key in this.snodes) {
        this.snodes[key].place();
    }
}

Graph.prototype.layout = function(node, depth, cx, cy, px, py, ang0, ang1) {
    node.x = px + ((Math.random() * 50) - 25);
    node.y = py + ((Math.random() * 50) - 25);

    var count = node.subNodes.length;

    var ang = ang0;
    var rad = depth * 150;

    var deltaAng = 0;
    if (count == 1) {
        ang += (ang1 - ang0) / 2;
        deltaAng = Math.PI;
    }
    else {
        if (depth == 1) {
            deltaAng = (ang1 - ang0) / count;
        }
        else {
            deltaAng = (ang1 - ang0) / (count - 1);
            deltaAng *= 0.75;
        }
    }

    var i;
    for (var i = 0; i < count; i++) {
        var npx = cx + (Math.sin(ang) * rad);
        var npy = cy + (Math.cos(ang) * rad);
        this.layout(node.subNodes[i], depth + 1, cx, cy, npx, npy, ang - (deltaAng / 2), ang + (deltaAng / 2));
        ang += deltaAng;
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

Graph.prototype.forceStep = function() {
    var drag = 0.85;
    var coulombConst = 300;
    var hookeConst = 0.06;

    // Init forces
    for (var key in this.snodes) {
        var snode = this.snodes[key];
        snode.fX = 0;
        snode.fY = 0;
    }

    // Coulomb repulsion
    for (var i = 0; i < this.snodeKeys.length; i++) {
        var orig = this.snodes[this.snodeKeys[i]];
        for (var j = i + 1; j < this.snodeKeys.length; j++) {
            var targ = this.snodes[this.snodeKeys[j]];

            var deltaX = orig.x - targ.x;
            var deltaY = orig.y - targ.y;
            var d2 = (deltaX * deltaX) + (deltaY * deltaY);
            /*var d2 = rectsDist2(orig.x1, orig.y1, orig.x2, orig.y2, targ.x1, targ.y1, targ.x2, targ.y2);
            if (d2 == 0) {
                d2 = 10000;
            }*/
            var fX = (deltaX / d2) * coulombConst;
            var fY = (deltaY / d2) * coulombConst;
            orig.fX += fX;
            orig.fY += fY;
            targ.fX -= fX;
            targ.fY -= fY;
        }
    }

    // Hooke attraction
    for (var i = 0; i < this.links.length; i++) {
        var link = this.links[i];
        var orig = link.sorig;
        var targ = link.starg;

        var deltaX = orig.x - targ.x;
        var deltaY = orig.y - targ.y;
        //var d2 = (deltaX * deltaX) + (deltaY * deltaY);
        var fX = deltaX * hookeConst;
        var fY = deltaY * hookeConst;
        orig.fX -= fX;
        orig.fY -= fY;
        targ.fX += fX;
        targ.fY += fY;
    }

    // Update velocities and positions
    for (var key in this.snodes) {
        var node = this.snodes[key];
        if (node.parent != '') {
            node.vX = (node.vX + node.fX) * drag;
            node.vY = (node.vY + node.fY) * drag;
            node.x = node.x + node.vX;
            node.y = node.y + node.vY;
        }
    }
}