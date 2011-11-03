// Graph
var Graph = function(context) {
    this.context = context;
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
    for (var key in g.nodes) {
        if (g.nodes.hasOwnProperty(key)) {
            g.nodes[key].place();
        }
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
    }
    else if (count > 1) {
        if (depth == 1) {
            deltaAng = (ang1 - ang0) / count;
        }
        else {
            deltaAng = (ang1 - ang0) / (count - 1);
            deltaAng *= 0.75;
        }
    }

    var i;
    for (i = 0; i < count; i++) {
        var npx = cx + (Math.sin(ang) * rad);
        var npy = cy + (Math.cos(ang) * rad);
        this.layout(node.subNodes[i], depth + 1, cx, cy, npx, npy, ang - (deltaAng / 2), ang + (deltaAng / 2));
        ang += deltaAng;
    }
}

Graph.prototype.labelAtPoint = function(x, y) {
    var p = [x, y];
    for (i = this.links.length - 1; i >= 0; i--) {
        if (this.links[i].pointInLabel(p)) {
            return this.links[i];
        }
    }

    return -1;
}