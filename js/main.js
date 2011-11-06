// Entry point functions & global variables
var g;
var uiMode;
var draggedNode;
var dragging;
var newLink;
var tipVisible;

var cycle = 0;
var ga = 0;

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

$(function() {
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
        var parentID = n['parent'];
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
    g.genNodeKeys();

    for (i = 0; i < links.length; i++) {
        var l = links[i];
        var link = new Link(l['id'], g.nodes[l['orig']], g.nodes[l['targ']], l['type']);
        g.links.push(link);
    }
    
    var halfWidth = window.innerWidth / 2;
    var halfHeight = window.innerHeight / 2;

    g.layout(g.root, 1, halfWidth, halfHeight, halfWidth, halfHeight, 0, Math.PI * 2);
    /*for (var key in g.nodes) {
        var node = g.nodes[key];
        node.x = halfWidth + ((Math.random() * 50) - 25);
        node.y = halfHeight + ((Math.random() * 50) - 25);
    }*/

    /*for (i = 0; i < 100; i++) {
        g.forceStep();
    }*/

    context.canvas.width  = window.innerWidth;
    context.canvas.height = window.innerHeight;

    g.drawLinks();
    g.placeNodes();

    graphAnim();
});