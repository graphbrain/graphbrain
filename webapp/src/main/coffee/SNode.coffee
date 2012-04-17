# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Super node
class SNode extends VisualObj
    constructor: (@id) ->
        super()

        #position before rotation
        @x = 0
        @y = 0
        @z = 0
        # position after rotation
        @rpos = Array(3)

        # auxiliary vector for rotation calcs
        @auxVec = new Array(3)

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

    updatePos: (_x, _y, _z) ->
        @x = _x;
        @y = _y;
        @z = _z;

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
        _x = @rpos[0]
        _y = @rpos[1]
        _z = @rpos[2] + g.zOffset
        if (!isNaN(_x) && !isNaN(_y) && !isNaN(_z))
            transformStr = 'translate3d(' + (_x - @halfWidth) + 'px,' + (_y - @halfHeight) + 'px,' + _z + 'px)'
            transformStr += ' scale(' + @scale + ')'
            $('div#' + @id).css('-webkit-transform', transformStr)
            $('div#' + @id).css('-moz-transform', transformStr)


    moveTo: (x, y, z) ->
        @updatePos(x, y, z)
        @updateTransform()


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