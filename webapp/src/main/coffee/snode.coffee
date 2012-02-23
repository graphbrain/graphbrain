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


    updatePos: (_x, _y) ->
        @x = _x;
        @y = _y;
        @z = 0;

        # rotation
        @auxVec[0] = @x - g.halfWidth
        @auxVec[1] = @y - g.halfHeight
        @auxVec[2] = 0

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


    moveTo: (x, y) ->
        @updatePos(x, y)

        transformStr = 'translate3d(' + (@rpos[0] - @halfWidth) + 'px,' + (@rpos[1] - @halfHeight) + 'px,' + @rpos[2] + 'px)'
        $('div#' + @id).css('-webkit-transform', transformStr)


    place: ->
        snode = document.createElement('div')
    
        nodesCount = 0
        nodesCount++ for key of @nodes when @nodes.hasOwnProperty(key)
        if nodesCount > 1
            snode.setAttribute('class', 'snode_' + @depth)
        else
            snode.setAttribute('class', 'snode1_' + @depth)
        snode.setAttribute('id', @id)
    
        nodesDiv = document.getElementById("nodesDiv")
        nodesDiv.appendChild(snode)

        # place nodes contained in this super node
        @nodes[key].place() for key of @nodes when @nodes.hasOwnProperty(key)

        _width = $('div#' + @id).outerWidth()
        _height = $('div#' + @id).outerHeight()
    
        @width = _width
        @height = _height
        @halfWidth = _width / 2
        @halfHeight = _height / 2
        @moveTo(@x, @y)

        # calc relative positions of nodes contained in this super node
        @nodes[key].calcPos() for key of @nodes when @nodes.hasOwnProperty(key) 

        nodeObj = this

        $('div#' + @id).bind 'mousedown', (e) =>
            if uiMode is 'drag'
                draggedNode = nodeObj
                false
            else
                newLink = new Link(0, nodeObj, false, '...')
                newLink.tx = e.pageX
                newLink.ty = e.pageY
                false

        $('div#' + @id).bind 'click', (e) =>
            if dragging
                dragging = false
                false
            else
                true

        $("div#" + @id).hover (e) =>
            if (newLink) then newLink.targ = nodeObj
        , (e) =>

    toString: ->
        return '{' + @nodes[key].text + ', ...}' for key of @nodes when @nodes.hasOwnProperty(key)