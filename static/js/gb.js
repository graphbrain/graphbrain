// Aux
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

// Node
var Node = function(id, text, type, snode) {
    this.id = id;
    this.text = text;
    this.type = type;
    this.x = 0;
    this.y = 0;
    this.vX = 0;
    this.vY = 0;
    this.subNodes = [];
    this.snode = snode;
}

Node.prototype.updatePos = function() {
    var nodeDiv = $('#' + this.id)
    var offset = nodeDiv.offset();
    this.x = offset.left + (this.width / 2);
    this.y = offset.top + (this.height / 2);
}

Node.prototype.place = function() {
    var node = document.createElement('div');
    node.setAttribute('class', 'node');
    node.setAttribute('id', this.id);
    if (this.type == 'text') {
        node.innerHTML = '<a href="/node/' + this.id + '" id="' + this.id + '">' + this.text + '</a>';
    }
    else if (this.type == 'image') {
        node.innerHTML = '<a href="/node/' + this.id + '" id="' + this.id + '"><img src="' + this.text + '" width="50px" /></a>';
    }
    var snodeDiv = document.getElementById(this.snode.id);
    snodeDiv.appendChild(node);

    var nodeDiv = $('#' + this.id)
    var width = nodeDiv.width();
    var height = nodeDiv.height();
    if (this.type == 'image') {
        height = 55;
    }
    
    this.width = width;
    this.height = height;
   
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
    this.nodes = [];
    this.subNodes = [];
    this.parent = 'unknown';
}

SNode.prototype.moveTo = function(x, y, redraw) {
    redraw = typeof(redraw) !== 'undefined' ? redraw : true;
    this.x = x;
    this.y = y;
    $('div#' + this.id).css('left', (this.x - (this.width / 2)) + 'px');
    $('div#' + this.id).css('top', (this.y - (this.height / 2)) + 'px');
    
    // update positions for nodes contained in this super node
    for (var key in this.nodes) {
        this.nodes[key].updatePos();
    }

    if (redraw) {
        g.drawLinks();
    }
}

SNode.prototype.place = function() {
    var snode = document.createElement('div');
    snode.setAttribute('class', 'snode');
    snode.setAttribute('id', this.id);
    
    var nodesDiv = document.getElementById("nodesDiv");
    nodesDiv.appendChild(snode);

    // place nodes contained in this super node
    for (var key in this.nodes) {
        this.nodes[key].place();
    }

    var width = $('div#' + this.id).width();
    var height = $('div#' + this.id).height();
    
    snode.setAttribute('style', 'left:' + (this.x - (width / 2)) + 'px; top:' + (this.y - (height / 2)) + 'px;');
    this.width = width;
    this.height = height;
   
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
var Link = function(id, orig, sorig, targ, starg, type) {
    this.id = id;
    this.orig = orig;
    this.sorig = sorig;
    this.targ = targ;
    this.starg = starg;
    this.type = type;
    this.ox = 0;
    this.oy = 0;
    this.tx = 0;
    this.ty = 0;
}

Link.prototype.draw = function(context) {
    var x0 = this.ox;
    var y0 = this.oy;
    if (this.orig) {
        x0 = this.orig.x;
        y0 = this.orig.y;
    }
    else if (this.sorig) {
        x0 = this.sorig.x;
        y0 = this.sorig.y;
    }
    var x1 = this.tx;
    var y1 = this.ty;
    if (this.targ) {
        x1 = this.targ.x;
        y1 = this.targ.y;
    }
    else if (this.starg) {
        x1 = this.starg.x;
        y1 = this.starg.y;
    }

    var cx = x0 + ((x1 - x0) / 2)
    var cy = y0 + ((y1 - y0) / 2)

    var slope = (y1 - y0) / (x1 - x0);
    var angle = Math.atan(slope);

    var color = '#FFD326';
    var textcolor = '#000'

    if (~this.type.indexOf('direct'))
        color = '#BEE512';
    else if (~this.type.indexOf('char'))
        color = '#DFFD59';
    else if (~this.type.indexOf('play'))
        color = '#FFFC26';
    else if (~this.type.indexOf('is')) {
        color = '#ED9107';
        textcolor = '#FFF'
    }

    context.strokeStyle = color;
    context.fillStyle = color;
    context.lineWidth = 0.7;
    context.beginPath();
    context.moveTo(x0, y0);
    context.lineTo(x1, y1);
    context.stroke();

    context.font = "10pt Sans-Serif";
    var dim = context.measureText(this.type);
    var width = dim.width + 6;
    var height = 18;
    
    var p = [[0, 0], [0, 0], [0, 0], [0,0], [0,0]];
    this.points = p;

    context.beginPath();
    if ((x0 < x1) || ((x0 == x1) && (y0 < y1))) {
        p[0][0] = -(width / 2);     p[0][1] = -(height / 2);
        p[1][0] = -(width / 2);     p[1][1] = (height / 2);
        p[2][0] = (width / 2);      p[2][1] = (height / 2);
        p[3][0] = (width / 2) + 6;  p[3][1] = 0;
        p[4][0] = (width / 2);      p[4][1] = -(height / 2);
    }
    else {
        p[0][0] = -(width / 2);     p[0][1] = -(height / 2);
        p[1][0] = -(width / 2) - 6; p[1][1] = 0;
        p[2][0] = -(width / 2);     p[2][1] = (height / 2);
        p[3][0] = (width / 2);      p[3][1] = (height / 2);
        p[4][0] = (width / 2);      p[4][1] = -(height / 2);
    }

    for (i = 0; i < 5; i++) {
        rotateAndTranslate(p[i], angle, cx, cy);
    }

    context.moveTo(p[0][0], p[0][1]);
    for (i = 1; i < 5; i++) {
        context.lineTo(p[i][0], p[i][1]);
    }
    
    context.closePath();
    context.fill();

    context.save();
    context.translate(cx, cy);
    context.rotate(angle);
    context.fillStyle = textcolor;
    context.textAlign = "center";
    context.textBaseline = "middle";
    context.fillText(this.type, 0, 0);
    context.restore();
}

Link.prototype.pointInLabel = function(p) {
    return (pointInTriangle(this.points[0], this.points[1], this.points[2], p)
        || pointInTriangle(this.points[2], this.points[3], this.points[4], p)
        || pointInTriangle(this.points[0], this.points[2], this.points[4], p));
}

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
    var coulombConst = 200;
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
// Entry point functions & global variables
var g;
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
    $("#nodesDiv").bind("mouseup", (function(e) {
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
    var context = elem.getContext('2d');

    g = new Graph(context);

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
    }
    
    var halfWidth = window.innerWidth / 2;
    var halfHeight = window.innerHeight / 2;

    g.layout(g.root, 1, halfWidth, halfHeight, halfWidth, halfHeight, 0, Math.PI * 2);

    context.canvas.width  = window.innerWidth;
    context.canvas.height = window.innerHeight;

    g.drawLinks();
    g.placeNodes();

    graphAnim();
}

$(function() {
    initInterface();
    initGraph(); 
});
