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
var Node = function(id, text, type) {
    this.id = id;
    this.text = text;
    this.type = type;
    this.x = 0;
    this.y = 0;
    this.subNodes = [];
}

Node.prototype.moveTo = function(x, y) {
    this.x = x;
    this.y = y;
    $('div#' + this.id).css('left', (this.x - (this.width / 2)) + 'px');
    $('div#' + this.id).css('top', (this.y - (this.height / 2)) + 'px');
    g.drawLinks();
}

Node.prototype.place = function() {
    var node = document.createElement('div');
    node.setAttribute('class', 'node');
    node.setAttribute('id', this.id);
    if (this.type == 0) {
        node.innerHTML = '<a href="/node/' + this.id + '" id="' + this.id + '">' + this.text + '</a>';
    }
    else if (this.type == 1) {
        node.innerHTML = '<a href="/node/' + this.id + '" id="' + this.id + '"><img src="' + this.text + '" width="50px" /></a>';
    }
    var nodesDiv = document.getElementById("nodesDiv");
    nodesDiv.appendChild(node);

    var width = $('div#' + this.id).width();
    var height = $('div#' + this.id).height();
    if (this.type == 1) {
        height = 55;
    }
    node.setAttribute('style', 'left:' + (this.x - (width / 2)) + 'px; top:' + (this.y - (height / 2)) + 'px;');
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
    function(e) {
        if (newLink) {
            newLink.targ = false;
        }
    });
}

// Link
var Link = function(id, orig, targ, type) {
    this.id = id;
    this.orig = orig;
    this.targ = targ;
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
    var x1 = this.tx;
    var y1 = this.ty;
    if (this.targ) {
        x1 = this.targ.x;
        y1 = this.targ.y;
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
}

// Entry point functions & global variables
var g;
var uiMode;
var draggedNode;
var dragging;
var newLink;

var initGraph = function(nodes, links) {

    newLink = false;
    draggedNode = false;
    dragging = false;
    curTargNode = false;

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
                $('#overlay').fadeIn(80, (function(e) {
                    $('#box').css({visibility:'visible'});
                    if (newLink.orig) {
                        $('#dNode1').html(newLink.orig.text);
                        $('#dNode1_id').val(newLink.orig.id);
                        $('#dNode1').css({display:'block'});
                        $('#dNode1In').css({display:'none'});
                    }
                    else {
                        $('#dNode1').css({display:'block'});
                        $('#dNode1In').css({display:'inline'});
                        $('#dNode1_id').val(-1);
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
                        $('#dNode2_id').val(-1);
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
                document.forms["delinkForm"].elements["link_id"].value = l.id;
                $("#delinkForm").submit();
            }
        }
    }));
    
    var elem = document.getElementById('graphCanvas');
    var context = elem.getContext('2d');

    g = new Graph(context);

    context.translate(0.5, 0.5)

    var i;
    for (i = 0; i < nodes.length; i++) {
        var n = nodes[i];
        var id = n['id'];
        var text = n['text'];
        var type = n['type'];
        var node = new Node(id, text, type);
        g.nodes[id] = node;
    }   

    // Assign root, parents and subNodes
    for (i = 0; i < nodes.length; i++) {
        var n = nodes[i];
        var id = n['id'];
        var parentID = n['parent'];
        if (parentID == '') {
            g.root = g.nodes[id];
            g.nodes[id].parent = false;
        }
        else {
            g.nodes[id].parent = g.nodes[parentID];
            g.nodes[parentID].subNodes[g.nodes[parentID].subNodes.length] = g.nodes[id];
        }
    }

    for (i = 0; i < links.length; i++) {
        var l = links[i];
        var link = new Link(l['id'], g.nodes[l['orig']], g.nodes[l['targ']], l['type']);
        g.links.push(link);
    }
    
    var halfWidth = window.innerWidth / 2;
    var halfHeight = window.innerHeight / 2;

    g.layout(g.root, 1, halfWidth, halfHeight, halfWidth, halfHeight, 0, Math.PI * 2);

    context.canvas.width  = window.innerWidth;
    context.canvas.height = window.innerHeight;

    g.drawLinks();
    g.placeNodes();
}
