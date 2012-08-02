# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Link
class Link
    constructor: (@id, @orig, @sorig, @targ, @starg, @label, @color) ->
        @ox = 0
        @oy = 0
        @tx = 0
        @ty = 0
        @len = 0

        # jquery objects
        @jqLabel = false

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
        $('#graph-view').append('<div class="linkLabel" id="linkLabel' + @id + '"><div class="linkText" id="linkText' + @id + '">' + @label + '</div><div class="linkArrow" id="linkArrow' + @id + '" /></div>')
        $('#graph-view').append('<div class="linkPoint" id="linkPoint1' + @id + '"></div>')
        $('#graph-view').append('<div class="linkPoint" id="linkPoint2' + @id + '"></div>')
        $('#graph-view').append('<div class="linkPoint" id="linkPoint3' + @id + '"></div>')
        $('#graph-view').append('<div class="linkPoint" id="linkPoint4' + @id + '"></div>')

        $('#linkText' + @id).css('background', @color)
        $('#linkArrow' + @id).css('border-left', '11px solid ' + @color)
        $('#linkPoint1' + @id).css('background', @color)
        $('#linkPoint2' + @id).css('background', @color)
        $('#linkPoint3' + @id).css('background', @color)
        $('#linkPoint4' + @id).css('background', @color)
        snode = @starg
        if snode == g.root
            snode = @sorig
        snode.jqDiv.css('border-color', @color)

        @jqLabel = $('#linkLabel' + @id)

        height = @jqLabel.outerHeight()
        @halfHeight = height / 2;
        labelWidth = @jqLabel.outerWidth()
        @halfLabelWidth = labelWidth / 2

    updatePoint: (pointId, pos) ->
        deltaX = @x1 - @x0
        deltaY = @y1 - @y0
        deltaZ = @z1 - @z0
        tx = @x0 + deltaX * pos
        ty = @y0 + deltaY * pos
        tz = @z0 + deltaZ * pos
        tx -= 1.5
        ty -= 1.5
        transformStr = 'translate3d(' + tx + 'px,' + ty + 'px,' + tz + 'px)'
        $(pointId).css('-webkit-transform', transformStr)
        $(pointId).css('-moz-transform', transformStr)

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

        @jqLabel.css('left', '' + ((len / 2) - @halfLabelWidth) + 'px')

        # apply translation to label
        tx = cx - (len / 2)
        ty = cy - @halfHeight
        tz = cz + g.zOffset
        transformStr = 'translate3d(' + tx + 'px,' + ty + 'px,' + tz + 'px)' + ' rotateZ(' + rotz + 'rad)' + ' rotateY(' + roty + 'rad)'
        @jqLabel.css('-webkit-transform', transformStr)
        @jqLabel.css('-moz-transform', transformStr)

        # apply translation to points
        @updatePoint('#linkPoint1' + @id, 0.1)
        @updatePoint('#linkPoint2' + @id, 0.2)
        @updatePoint('#linkPoint3' + @id, 0.8)
        @updatePoint('#linkPoint4' + @id, 0.9)

        z = cz
        if z < 0
            opacity = -1 / (z * 0.007)
            @jqLabel.css('opacity', opacity)
            $('#linkPoint1' + @id).css('opacity', opacity)
            $('#linkPoint2' + @id).css('opacity', opacity)
            $('#linkPoint3' + @id).css('opacity', opacity)
            $('#linkPoint4' + @id).css('opacity', opacity)
        else
            @jqLabel.css('opacity', 0.9)
            $('#linkPoint1' + @id).css('opacity', 0.7)
            $('#linkPoint2' + @id).css('opacity', 0.7)
            $('#linkPoint3' + @id).css('opacity', 0.7)
            $('#linkPoint4' + @id).css('opacity', 0.7)