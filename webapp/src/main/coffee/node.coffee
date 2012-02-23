# (c) 2012 GraphBrain Ltd. All rigths reserved.

nodeCount = 0

# Node
class Node
    constructor: (@id, @text, @type, @snode) ->
        @divid = 'n' + nodeCount++
        @rpos = Array(3)
        @subNodes = []

        # position in relation to super node
        @sx = 0
        @sy = 0

    calcPos: ->
        nodeDiv = $('#' + this.divid)
        offset = nodeDiv.offset()
        @rpos[0] = offset.left + @halfWidth
        @rpos[1] = offset.top + @halfHeight
        @rpos[2] = 0
        @x0 = @rpos[0] - @halfWidth
        @y0 = @rpos[1] - @halfHeight
        @x1 = @rpos[0] + @halfWidth
        @y1 = @rpos[1] + @halfHeight

        @sx = @rpos[0] - @snode.x - @snode.halfWidth
        @sy = @rpos[1] - @snode.y - @snode.halfHeight

    estimatePos: ->
        @rpos[0] = @snode.rpos[0] + @sx
        @rpos[1] = @snode.rpos[1] + @sy
        @rpos[2] = @snode.rpos[2]

        @x0 = @rpos[0] - @halfWidth
        @y0 = @rpos[1] - @halfHeight
        @x1 = @rpos[0] + @halfWidth
        @y1 = @rpos[1] + @halfHeight

    place: ->
        node = document.createElement('div')
        node.setAttribute('class', 'node_' + @snode.depth)
        node.setAttribute('id', @divid)
        if @type == 'text'
            node.innerHTML = '<a href="/node/' + @id + '" id="' + @divid + '">' + @text + '</a>'
        else if @type == 'image'
            node.innerHTML = '<a href="/node/' + @id + '" id="' + @divid + '"><img src="' + @text + '" width="50px" /></a>'
        snodeDiv = document.getElementById(@snode.id)
        snodeDiv.appendChild(node)

        nodeDiv = $('#' + @divid)
        _width = nodeDiv.outerWidth()
        _height = nodeDiv.outerHeight()
        # TODO: temporary hack
        if this.type == 'image'
            _width = 50
            _height = 80
    
        @width = _width
        @height = _height
        @halfWidth = _width / 2
        @halfHeight = _height / 2