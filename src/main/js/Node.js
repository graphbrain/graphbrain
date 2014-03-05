var Node, nodeCount;

nodeCount = 0;

function getHostname(url) {
    var m = ((url||'')+'').match(/^http:\/\/([^/]+)/);
    return m ? m[1] : null;
};


Node = (function() {

  function Node(id, text, text2, type, snode, edge, url, icon, glow) {
    this.id = id;
    this.text = text;
    this.text2 = text2;
    this.type = type;
    this.snode = snode;
    this.edge = edge;
    this.url = url != null ? url : '';
    this.icon = icon != null ? icon : '';
    this.glow = glow != null ? glow : false;
    this.divid = 'n' + nodeCount++;
    this.root = false;
  }

  Node.prototype.place = function() {
    var html, nodeData, nodeTitleClass, nodeUrlClass, removeData, removeLinkId;
    if (this.root) {
      $('#' + this.snode.id + ' .viewport').append('<div id="' + this.divid + '" class="node_root" />');
    } else {
      $('#' + this.snode.id + ' .viewport').append('<div id="' + this.divid + '" class="node" />');
    }
    nodeData = {};
    if (this.snode.relpos === 0) {
      nodeData = {
        'node': this.id,
        'orig': rootNodeId,
        'etype': this.snode.etype,
        'link': this.snode.label,
        'targ': this.id
      };
    } else {
      nodeData = {
        'node': this.id,
        'targ': rootNodeId,
        'etype': this.snode.etype,
        'link': this.snode.label,
        'orig': this.id
      };
    }
    removeLinkId = '';
    nodeTitleClass = 'nodeTitle';
    nodeUrlClass = 'nodeUrl';
    if (this.root) {
      nodeTitleClass = 'nodeTitle_root';
      nodeUrlClass = 'nodeUrl_root';
    }
    if (this.type === 'url') {
      html = '<div class="' + nodeTitleClass + '" id="t' + this.divid + '"><a href="/node/' + this.id + '" id="' + this.divid + '">' + this.text + '</a></div><br />';
      if (this.icon !== '') {
        html += '<img src="' + this.icon + '" width="16px" height="16px" class="nodeIco" />';
      }
      html += '<div class="' + nodeUrlClass + '"><a href="' + this.url + '" id="url' + this.divid + '">' + this.url + '</a></div>';
      if (!this.root) {
        removeLinkId = 'rem' + this.divid;
        html += '<div class="nodeRemove"><a id="' + removeLinkId + '" href="#">x</a></div>';
      }
      html += '<div style="clear:both;"></div>';
      $('#' + this.divid).append(html);
    } else {
      html = '<div class="' + nodeTitleClass + '" id="t' + this.divid + '"><a href="/node/' + this.id + '" id="' + this.divid + '">' + this.text + '</a></div>';
      if (this.text2 != null) {
        html += '<div class="nodeSubText">(' + this.text2 + ')</div>';
      }
      if (!this.root) {
        removeLinkId = 'rem' + this.divid;
        html += '<div class="nodeRemove"><a id="' + removeLinkId + '" href="#">x</a></div>';
      }
      html += '<div style="clear:both;"></div>';
      $('#' + this.divid).append(html);
    }
    if (removeLinkId !== '') {
      removeData = {
        'node': this,
        'link': this.snode.label,
        'edge': this.edge
      };
      $('#' + removeLinkId).click(removeData, removeClicked);
    }
    if (this.glow) {
      return addAnim(new AnimNodeGlow(this));
    }
  };

  return Node;

})();
