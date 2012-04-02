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

        @dlevel = 0


    calcPos: ->
        nodeDiv = $('#' + this.divid)
        @rpos[0] = @halfWidth
        @rpos[1] = @halfHeight
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
        node.setAttribute('class', 'node')
        node.setAttribute('id', @divid)
        
        node.innerHTML = '<a href="/node/' + @id + '" id="' + @divid + '">' + @text + '</a>'
        snodeDiv = document.getElementById(@snode.id)
        snodeDiv.appendChild(node)

        nodeDiv = $('#' + @divid)
        _width = nodeDiv.outerWidth()
        _height = nodeDiv.outerHeight()
    
        @width = _width
        @height = _height
        @halfWidth = _width / 2
        @halfHeight = _height / 2


    updateDetailLevel: (scale, z, depth) ->
        k = scale * (z + 500)
        _dlevel = 1
        if k < 1000
            _dlevel = 0
        #console.log(@text + '>> scale: ' + scale + '; z: ' + z + '; dlevel: ' + dlevel)
        
        if _dlevel == @dlevel
            return false

        @dlevel = _dlevel

        if @dlevel == 0
            $('div#' + @divid).css('font-size', '12px')
        else if @dlevel == 1
            $('div#' + @divid).css('font-size', '24px')

        return true