# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Link
class Link extends VisualObj
    constructor: (@id, @orig, @sorig, @targ, @starg, @label) ->
        super()
        @ox = 0
        @oy = 0
        @tx = 0
        @ty = 0
        @len = 0

    updatePos: ->
        _orig = false
        _targ = false
        origSuper = false
        targSuper = false

        if @orig
            _orig = @orig
        else if @sorig
            _orig = @sorig
            @origSuper = true
        
        if @targ
            _targ = @targ
        else if @starg
            _targ = @starg
            @targSuper = true

        x0 = _orig.rpos[0]
        y0 = _orig.rpos[1]
        x1 = _targ.rpos[0]
        y1 = _targ.rpos[1]

        p0 = interRect(x0, y0, x1, y1, _orig.x0, _orig.y0, _orig.x1, _orig.y1)
        p1 = interRect(x1, y1, x0, y0, _targ.x0, _targ.y0, _targ.x1, _targ.y1)

        @x0 = p0[0]
        @y0 = p0[1]
        @z0 = _orig.rpos[2]
        @x1 = p1[0]
        @y1 = p1[1]
        @z1 = _targ.rpos[2]

        # calc length
        _dx = @x1 - @x0
        _dy = @y1 - @y0
        @dx = _dx
        @dy = _dy
        @len = (_dx * _dx) + (_dy * _dy)
        @len = Math.sqrt(@len)

        # calc center
        @cx = @x0 + ((@x1 - @x0) / 2)
        @cy = @y0 + ((@y1 - @y0) / 2)

        slope = (@y1 - @y0) / (@x1 - @x0)
        @angle = Math.atan(slope)

    place: ->
        # create link div
        linkDiv = document.createElement('div')
        linkDiv.setAttribute('class', 'link')
        linkDiv.setAttribute('id', 'link' + @id)
        
        nodesDiv = document.getElementById("nodesDiv")
        nodesDiv.appendChild(linkDiv)

        $('#link' + @id).append('<div class="linkLine" id="linkLine' + @id + '"></div>')
        $('#link' + @id).append('<div class="linkLabel" id="linkLabel' + @id + '"><div class="linkText">' + @label + '</div><div class="linkArrow" /></div>')

        _height = $('#link' + @id).outerHeight()
        @halfHeight = _height / 2;
        labelWidth = $('#linkLabel' + @id).outerWidth()
        @halfLabelWidth = labelWidth / 2

        $('#linkLine' + @id).css('top', '' + @halfHeight + 'px')


    visualUpdate: ->
        deltaX = @x1 - @x0
        deltaY = @y1 - @y0
        deltaZ = @z1 - @z0
        
        cx = @x0 + (deltaX / 2)
        cy = @y0 + (deltaY / 2)
        cz = @z0 + (deltaZ / 2)

        len = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ))

        rotz = Math.atan2(deltaY, deltaX)
        roty = 0
        if deltaX >= 0
            roty = -Math.atan2(deltaZ * Math.cos(rotz), deltaX)
        else
            roty = Math.atan2(deltaZ * Math.cos(rotz), -deltaX)

        $('#link' + @id).css('width', '' + len + 'px')
        $('#linkLine' + @id).css('height', '1px')
        $('#linkLabel' + @id).css('left', '' + ((len / 2) - @halfLabelWidth) + 'px')

        # apply translation
        tx = cx - (len / 2)
        ty = cy - @halfHeight
        tz = cz + g.zOffset
        
        transformStr = 'translate3d(' + tx + 'px,' + ty + 'px,' + tz + 'px)' + ' rotateZ(' + rotz + 'rad)' + ' rotateY(' + roty + 'rad)'
        $('#link' + @id).css('-webkit-transform', transformStr)
        $('#link' + @id).css('-moz-transform', transformStr)