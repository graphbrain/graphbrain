/**
 * (c) 2012 GraphBrain Ltd. All rigths reserved.
 */

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

    // auxiliary quaternions and matrices for 3D rotation
    this.quat = new Quaternion();
    this.deltaQuat = new Quaternion();
    this.affinMat = new Array(16);
    this.quat.getMatrix(this.affinMat);
}

Graph.prototype.rotateX = function(angle) {
    this.deltaQuat.fromEuler(angle, 0, 0);
    this.quat.mul(this.deltaQuat);
    this.quat.normalise();
    this.quat.getMatrix(this.affinMat);
}

Graph.prototype.rotateY = function(angle) {
    this.deltaQuat.fromEuler(0, 0, angle);
    this.quat.mul(this.deltaQuat);
    this.quat.normalise();
    this.quat.getMatrix(this.affinMat);
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