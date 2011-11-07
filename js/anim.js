// Graph animation cycle

var cycle = 0;

var graphAnim = function() {
    for (var i = 0; i < 20; i++) {
        g.forceStep();
    }

    for (var key in g.nodes) {
        var node = g.nodes[key];
        node.moveTo(node.x, node.y, false);
    }
    g.drawLinks();
    cycle += 1;
    if (cycle < 5) {
        setTimeout('graphAnim()', 100);
    }
}