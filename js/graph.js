// Graph
var Graph = function() {
    this.snodes = {}
    this.nodes = {};
    this.links = [];
    this.newNode = false;
    this.newNodeActive = false;
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

Graph.prototype.layoutSNode = function(snode, fixedSNodes, width, height) {
    var iters = 100;

    snode.fixed = true;

    var bestPenalty = 99999999;
    var bestX, bestY;
    
    for (var i = 0; i < iters; i++) {
        var penalty = 0;

        var x = Math.random() * width;
        var y = Math.random() * height;
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
        }

        // node-label overlap penalty
        for (var k = 0; k < this.links.length; k++) {
            var link = this.links[k];
            if (link.sorig.fixed && link.starg.fixed)
                if (snode.overlaps(link))
                    penalty += 10000;
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
    }

    console.log("best: " + bestPenalty);

    snode.moveTo(bestX, bestY);
}

Graph.prototype.layout2 = function(width, height) {
    // set all super nodes non-fixed
    for (var key in this.snodes) {
        var snode = this.snodes[key];
        snode.fixed = false;
    }

    // layout root node
    var fixedSNodes = [g.root];
    g.root.moveTo(width / 2, height / 2);
    g.root.fixed = true;
    
    // layout tier 1 nodes
    for (var key in this.snodes) {
        var snode = this.snodes[key];
        if (snode.parent == g.root) {
            this.layoutSNode(snode, fixedSNodes, width, height);
            fixedSNodes.push(snode);
        }
    }

    // layout tier 2 nodes
    for (var key in this.snodes) {
        var snode = this.snodes[key];
        if ((snode.parent != g.root) && (snode.parent != '')){
            this.layoutSNode(snode, fixedSNodes, width, height);
            fixedSNodes.push(snode);
        }
    }
}