// Graph animation cycle

var cycle = 0;

var graphAnim = function() {
    for (var i = 0; i < 20; i++) {
        g.forceStep();
    }

    for (var key in g.snodes) {
        var snode = g.snodes[key];
        snode.moveTo(snode.x, snode.y, false);
    }
    g.drawLinks();
    cycle += 1;
    if (cycle < 5) {
        setTimeout('graphAnim()', 100);
    }
}