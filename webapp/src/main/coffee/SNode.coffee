# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Super node
class SNode
    constructor: (@id, @etype, @relpos, @label, @color, @isRoot) ->
        @initLayout()

        @nodes = {}

        @width = 0
        @height = 0
        @halfWidth = 0
        @halfHeight = 0
        @scale = 1

        # jquery objects
        @jqDiv = false


    initLayout: ->
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


    updateTransform: ->
        x = @rpos[0]
        y = @rpos[1]
        z = @rpos[2] + g.zOffset
        if (!isNaN(x) && !isNaN(y) && !isNaN(z))
            transformStr = 'translate3d(' + (x - @halfWidth) + 'px,' + (y - @halfHeight) + 'px,' + z + 'px)'
            transformStr += ' scale(' + @scale + ')'
            @jqDiv.css('-webkit-transform', transformStr)
            @jqDiv.css('-moz-transform', transformStr)
            if z < 0
                opacity = -1 / (z * 0.007)
                @jqDiv.css('opacity', opacity)
            else
                @jqDiv.css('opacity', 1)


    moveTo: (x, y, z) ->
        @x = x
        @y = y
        @z = z

        # rotation
        @auxVec[0] = @x
        @auxVec[1] = @y
        @auxVec[2] = @z

        m4x4mulv3(g.affinMat, @auxVec, @rpos)
    
        # sphere mapping
        sc = new SphericalCoords
        sc.x = @rpos[0]
        sc.y = @rpos[1]
        sc.z = @rpos[2]
        sc.cartesianToSpherical()
        sc.viewMapping()
        #console.log(@toString() + '; r: ' + sc.r +  '; theta: ' + sc.theta + '; phi: ' + sc.phi)
        sc.sphericalToCartesian()
        @rpos[0] = sc.x
        @rpos[1] = sc.y
        @rpos[2] = sc.z

        # convert to screen coordinates
        @rpos[0] = @rpos[0] * g.halfWidth * 0.8 + g.halfWidth
        @rpos[1] += @rpos[1] * g.halfHeight * 0.8 + g.halfHeight
        @rpos[2] += @rpos[2] * Math.min(g.halfWidth, g.halfHeight) * 0.8

        # update position of connected links
        @updateTransform()


    applyPos: ->
        @moveTo(@pos[0], @pos[1], @pos[2])


    updateDimensions: ->
        @width = @jqDiv.outerWidth()
        @height = @jqDiv.outerHeight()
        @halfWidth = @width / 2
        @halfHeight = @height / 2

        if @initialWidth < 0
            @initialWidth = @width

        @updateTransform()


    place: ->
        html = '<div id="' + @id + '" class="snode">'
        relText = ''
        if not @isRoot
            rootText = g.rootNode['text']
            relText = @label
            if @relpos == 1
                relText += ' ' + rootText
        html += '<div class="snodeLabel">' + relText + '</div>'
        if @isRoot
            html += '<div class="snodeInner snodeRoot">'
        else
            html += '<div class="snodeInner">'
        html += '<div class="viewport" /></div></div>'
        $('#graph-view').append(html)

        @jqDiv = $('#' + @id)

        # place nodes contained in this super node
        @nodes[key].place() for key of @nodes when @nodes.hasOwnProperty(key)

        # scrollbar
        if @jqDiv.outerHeight() > 250
            $('#' + @id + ' .viewport').slimScroll({height: '250px'})
            @jqDiv.hover scrollOn, scrollOff

        @updateDimensions()

        if not @isRoot
            @setColor(@color)


    setColor: (color) ->
        $('#' + @id + ' .snodeInner').css('border-color', color)
        $('#' + @id + ' .snodeLabel').css('background', color)


    toString: ->
        return '{' + @nodes[key].text + ', ...}' for key of @nodes when @nodes.hasOwnProperty(key)
