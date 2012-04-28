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

    estimatePos: ->
        @rpos[0] = @snode.rpos[0] + @sx
        @rpos[1] = @snode.rpos[1] + @sy
        @rpos[2] = @snode.rpos[2]

        @x0 = @rpos[0] - @halfWidth
        @y0 = @rpos[1] - @halfHeight
        @x1 = @rpos[0] + @halfWidth
        @y1 = @rpos[1] + @halfHeight

    updateDimensions: ->
        nodeDiv = $('#' + @divid) 
        @width = nodeDiv.outerWidth()
        @height = nodeDiv.outerHeight()
        @halfWidth = @width / 2
        @halfHeight = @height / 2

    place: ->
        # create node div
        $('#' + @snode.id + ' .viewport').append('<div id="' + @divid + '" class="node" />')

        # create title div
        html = '<div id="t' + @divid + '"><a href="/node/' + @id + '" id="' + @divid + '">' + @text + '</a></div>'
        $('#' + @divid).append(html)

        # create detail div
        html = '<div id="d' + @divid + '" class="nodeDetail">Some more text about this node.</div>'
        $('#' + @divid).append(html)

        @updateDimensions()

    updateDetailLevel: (scale, z, depth) ->
        k = scale * (z + 500)
        _dlevel = 1
        if k < 99999999 #1000
            _dlevel = 0
        #console.log(@text + '>> scale: ' + scale + '; z: ' + z + '; dlevel: ' + dlevel)
        
        if _dlevel == @dlevel
            return false

        @dlevel = _dlevel

        if @dlevel == 0
            $('div#' + @divid).css('font-size', '12px')
            $('div#d' + @divid).css('display', 'none')
        else if @dlevel == 1
            $('div#' + @divid).css('font-size', '24px')
            $('div#d' + @divid).css('display', 'block')

        return true