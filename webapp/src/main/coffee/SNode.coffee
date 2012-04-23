# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Super node
class SNode
    constructor: (@id) ->
        #position before rotation
        @pos = newv3()
        @x = 0
        @y = 0
        @z = 0
        # position after rotation
        @rpos = Array(3)

        # auxiliary vector for rotation calcs
        @auxVec = new Array(3)

        # layout
        @f = newv3()    # force
        @tpos = newv3() # temporary position

        @nodes = {}
        @subNodes = []
        @parent = 'unknown'
        @links = []
        @weight = 0
        @depth = 0

        @width = 0
        @height = 0
        @halfWidth = 0
        @halfHeight = 0
        @initialWidth = -1
        @scale = 1

        # bounding rectangle
        @rect = []
        @rect.v1 = []
        @rect.v2 = []
        @rect.v3 = []
        @rect.v4 = []

        @rect.v1.x = 0
        @rect.v1.y = 0
        @rect.v1.z = 0
        @rect.v2.x = 0
        @rect.v2.y = 0
        @rect.v2.z = 0
        @rect.v3.x = 0
        @rect.v3.y = 0
        @rect.v3.z = 0
        @rect.v4.x = 0
        @rect.v4.y = 0
        @rect.v4.z = 0

    updatePos: (x, y, z) ->
        @x = x
        @y = y
        @z = z

        # rotation
        @auxVec[0] = @x - g.halfWidth
        @auxVec[1] = @y - g.halfHeight
        @auxVec[2] = @z

        m4x4mulv3(g.affinMat, @auxVec, @rpos)
    
        @rpos[0] += g.halfWidth
        @rpos[1] += g.halfHeight

        # limits used to place links
        @x0 = @rpos[0] - @halfWidth
        @y0 = @rpos[1] - @halfHeight
        @x1 = @rpos[0] + @halfWidth
        @y1 = @rpos[1] + @halfHeight
    
        # calc bounding rectangle
        @rect.v1.x = @rpos[0] - @halfWidth
        @rect.v1.y = @rpos[1] - @halfHeight
        @rect.v2.x = @rpos[0] - @halfWidth
        @rect.v2.y = @rpos[1] + @halfHeight
        @rect.v3.x = @rpos[0] + @halfWidth
        @rect.v3.y = @rpos[1] + @halfHeight
        @rect.v4.x = @rpos[0] + @halfWidth
        @rect.v4.y = @rpos[1] - @halfHeight

        # update position of contained nodes
        @nodes[key].estimatePos() for key of @nodes when @nodes.hasOwnProperty(key) 

        # update position of connected links
        link.updatePos() for link in @links


    updateTransform: ->
        x = @rpos[0]
        y = @rpos[1]
        z = @rpos[2] + g.zOffset
        if (!isNaN(x) && !isNaN(y) && !isNaN(z))
            transformStr = 'translate3d(' + (x - @halfWidth) + 'px,' + (y - @halfHeight) + 'px,' + z + 'px)'
            transformStr += ' scale(' + @scale + ')'
            $('div#' + @id).css('-webkit-transform', transformStr)
            $('div#' + @id).css('-moz-transform', transformStr)
            if z < 0
                opacity = -1 / (z * 0.007)
                $('div#' + @id).css('opacity', opacity)
            else
                $('div#' + @id).css('opacity', 1)


    moveTo: (x, y, z) ->
        @updatePos(x, y, z)
        @updateTransform()


    applyPos: ->
        @x = @pos[0] * (g.halfWidth * 0.8) + g.halfWidth
        @y = @pos[1] * (g.halfHeight * 0.8) + g.halfHeight
        @z = @pos[2] * Math.min(g.halfWidth, g.halfHeight) * 0.8
        @moveTo(@x, @y, @z)


    updateDimensions: ->
        @width = $('div#' + @id).outerWidth()
        @height = $('div#' + @id).outerHeight()
        @halfWidth = @width / 2
        @halfHeight = @height / 2

        if @initialWidth < 0
            @initialWidth = @width

        @updateTransform()

        # calc relative positions of nodes contained in this super node
        @nodes[key].updateDimensions() for key of @nodes when @nodes.hasOwnProperty(key)


    place: ->
        html = '<div id="' + @id + '"><div class="viewport" /></div>'
        $('#nodesDiv').append(html)

        nodesCount = 0
        nodesCount++ for key of @nodes when @nodes.hasOwnProperty(key)
        if nodesCount > 1
            $('#' + @id).addClass('snodeN')
        else
            $('#' + @id).addClass('snode1')

        # place nodes contained in this super node
        @nodes[key].place() for key of @nodes when @nodes.hasOwnProperty(key)

        # scrollbar
        if (nodesCount > 1) && ($('div#' + @id).outerHeight() > 250)
            $('#' + @id + ' .viewport').slimScroll({height: '250px'})
            $('#' + @id).hover scrollOn, scrollOff

        @updateDimensions()

        nodeObj = this


    updateDetailLevel: (scale) ->
        updated = false
        for key of @nodes
            if @nodes.hasOwnProperty(key)
                if @nodes[key].updateDetailLevel(scale, @rpos[2], @depth)
                    updated = true

        if updated
            @updateDimensions()
            @scale = @initialWidth / @width
            #console.log('initialWidth: ' + @initialWidth + '; width: ' + @width + '; scale: ' + @scale)
            @updateTransform()


    toString: ->
        return '{' + @nodes[key].text + ', ...}' for key of @nodes when @nodes.hasOwnProperty(key)