# (c) 2012 GraphBrain Ltd. All rigths reserved.

nodeCount = 0

`function getHostname(url) {
    var m = ((url||'')+'').match(/^http:\/\/([^/]+)/);
    return m ? m[1] : null;
}`

# Node
class Node
    constructor: (@id, @text, @type, @snode, @edge, @url='', @icon='', @glow=false) ->
        @divid = 'n' + nodeCount++
        @root = false

    place: ->
        # create node div
        if @root
            $('#' + @snode.id + ' .viewport').append('<div id="' + @divid + '" class="node_root" />')
        else
            $('#' + @snode.id + ' .viewport').append('<div id="' + @divid + '" class="node" />')

        nodeData = {}
        if @snode.relpos == 0
            nodeData = {'node': @id, 'orig': rootNodeId, 'etype': @snode.etype, 'link': @snode.label, 'targ': @id}
        else
            nodeData = {'node': @id, 'targ': rootNodeId, 'etype': @snode.etype, 'link': @snode.label, 'orig': @id}

        # create url div
        removeLinkId = ''
        if @type == 'url'
            html = ''
            if @icon != ''
                html += '<img src="' + @icon + '" width="16px" height="16px" class="nodeIco" />'
            html += '<div class="nodeUrl"><a href="' + @url + '" id="url' + @divid + '">' + @url + '</a></div>'
            if not @root
                removeLinkId = 'rem' + @divid
                html += '<div class="nodeRemove"><a id="' + removeLinkId + '" href="#">x</a></div>'
            html += '<div style="clear:both;"></div>'
            $('#' + @divid).append(html)
        else
            html = '<div class="nodeTitle" id="t' + @divid + '"><a href="/node/' + @id + '" id="' + @divid + '">' + @text + '</a></div>'
            if not @root
                removeLinkId = 'rem' + @divid
                html += '<div class="nodeRemove"><a id="' + removeLinkId + '" href="#">x</a></div>'
            html += '<div style="clear:both;"></div>'
            $('#' + @divid).append(html)

        if removeLinkId != ''
            removeData = {'node': this, 'link': @snode.label, 'edge': @edge}
            $('#' + removeLinkId).click(removeData, removeClicked)

        if @glow
            addAnim(new AnimNodeGlow(this))