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
    if (this.type == 'text') {
        node.innerHTML = '<a href="/node/' + this.id + '" id="' + this.id + '">' + this.text + '</a>';
    }
    else if (this.type == 'image') {
        node.innerHTML = '<a href="/node/' + this.id + '" id="' + this.id + '"><img src="' + this.text + '" width="50px" /></a>';
    }
    var nodesDiv = document.getElementById("nodesDiv");
    nodesDiv.appendChild(node);

    var width = $('div#' + this.id).width();
    var height = $('div#' + this.id).height();
    if (this.type == 'image') {
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
    function(e) {});
}
