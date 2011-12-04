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
    var overlap = rotRectsOverlap(r1, r2);
    console.log(overlap);

    initInterface();
    initGraph();
});