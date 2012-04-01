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


    moveTo: (x, y, z) ->
        @updatePos(x, y, z)

        _x = @rpos[0]
        _y = @rpos[1]
        _z = @rpos[2] + g.zOffset

        transformStr = 'translate3d(' + (_x - @halfWidth) + 'px,' + (_y - @halfHeight) + 'px,' + _z + 'px)'
        $('div#' + @id).css('-webkit-transform', transformStr)


    updateDimensions: ->
        _width = $('div#' + @id).outerWidth()
        _height = $('div#' + @id).outerHeight()
    
        @width = _width
        @height = _height
        @halfWidth = _width / 2
        @halfHeight = _height / 2
        @moveTo(@x, @y, @z)

        # calc relative positions of nodes contained in this super node
        @nodes[key].calcPos() for key of @nodes when @nodes.hasOwnProperty(key)


    place: ->
        snode = document.createElement('div')
    
        nodesCount = 0
        nodesCount++ for key of @nodes when @nodes.hasOwnProperty(key)
        if nodesCount > 1
            snode.setAttribute('class', 'snode1')
        else
            snode.setAttribute('class', 'snodeN')
        snode.setAttribute('id', @id)
    
        nodesDiv = document.getElementById("nodesDiv")
        nodesDiv.appendChild(snode)

        # place nodes contained in this super node
        @nodes[key].place() for key of @nodes when @nodes.hasOwnProperty(key)

        @updateDimensions()

        nodeObj = this


    toString: ->
        return '{' + @nodes[key].text + ', ...}' for key of @nodes when @nodes.hasOwnProperty(key)