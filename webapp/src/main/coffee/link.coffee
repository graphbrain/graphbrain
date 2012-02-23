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

        p = [[0, 0], [0, 0], [0, 0], [0, 0], [0, 0]]

        if (@x0 < @x1) || ((@x0 == @x1) && (@y0 < @y1))
            p[0][0] = -@halfWidth;     p[0][1] = -@halfHeight
            p[1][0] = -@halfWidth;     p[1][1] = @halfHeight
            p[2][0] = @halfWidth;      p[2][1] = @halfHeight
            p[3][0] = @halfWidth + 6;  p[3][1] = 0
            p[4][0] = @halfWidth;      p[4][1] = -@halfHeight
        else
            p[0][0] = -@halfWidth;     p[0][1] = -@halfHeight
            p[1][0] = -@halfWidth - 6; p[1][1] = 0
            p[2][0] = -@halfWidth;     p[2][1] = @halfHeight
            p[3][0] = @halfWidth;      p[3][1] = @halfHeight
            p[4][0] = @halfWidth;      p[4][1] = -@halfHeight

        i = 0
        while i < 5
            rotateAndTranslate(p[i], @angle, @cx, @cy)
            i++

        @points = p

        # calc bounding rectangle
        if (@x0 < @x1) || ((@x0 == @x1) && (@y0 < @y1))
            @rect.v1.x = p[0][0]
            @rect.v1.y = p[0][1]
            @rect.v2.x = p[1][0]
            @rect.v2.y = p[1][1]
            @rect.v3.x = p[2][0]
            @rect.v3.y = p[2][1]
            @rect.v4.x = p[4][0]
            @rect.v4.y = p[4][1]
        else
            @rect.v1.x = p[0][0]
            @rect.v1.y = p[0][1]
            @rect.v2.x = p[2][0]
            @rect.v2.y = p[2][1]
            @rect.v3.x = p[3][0]
            @rect.v3.y = p[3][1]
            @rect.v4.x = p[4][0]
            @rect.v4.y = p[4][1]


#    draw: ->
#        @updatePos()
# 
#        color = '#FFD326'
#        textcolor = '#000'
#
#        if (~@label.indexOf('direct'))
#            color = '#BEE512';
#        else if (~@label.indexOf('char'))
#            color = '#DFFD59';
#        else if (~@label.indexOf('play'))
#            color = '#FFFC26';
#        else if (~@label.indexOf('is')) {
#            color = '#ED9107';
#            textcolor = '#FFF'
#        }
#
#        // draw line
#        context.strokeStyle = color;
#        context.fillStyle = color;
#        context.lineWidth = 0.7;
#
#        context.beginPath();
#        context.moveTo(@x0, @y0);
#        context.lineTo(@x1, @y1);
#        context.stroke();
#
#        // draw circles at both ends of line
#        if (@origSuper) {
#            context.fillStyle = '#505050';
#        }
#        else {
#            context.fillStyle = color;   
#        }
#        context.beginPath();
#        var radius = 4;
#        context.arc(@x0, @y0, radius, 0, 2 * Math.PI, false);
#        context.fill();
#        if (@targSuper) {
#            context.fillStyle = '#505050';
#        }
#        else {
#            context.fillStyle = color;   
#        }
#        context.beginPath();
#        context.arc(@x1, @y1, radius, 0, 2 * Math.PI, false);
#        context.fill();
#        context.fillStyle = color;
#
#        // draw label area
#        context.beginPath();
#        context.moveTo(@points[0][0], @points[0][1]);
#        for (i = 1; i < 5; i++) {
#            context.lineTo(@points[i][0], @points[i][1]);
#        }
#        
#        context.closePath();
#        context.fill();
#
#        // draw label text
#        context.save();
#        context.translate(@cx, @cy);
#        context.rotate(@angle);
#        context.font = "10pt Sans-Serif";
#        context.fillStyle = textcolor;
#        context.textAlign = "center";
#        context.textBaseline = "middle";
#        context.fillText(@label, 0, 0);
#        context.restore();
#    }

    pointInLabel: ->
        pointInTriangle(@points[0], @points[1], @points[2], p) or pointInTriangle(@points[2], @points[3], @points[4], p) or pointInTriangle(@points[0], @points[2], @points[4], p)


    intersectsLink: (link2) ->
        lineSegsOverlap(@x0, @y0, @x1, @y1, link2.x0, link2.y0, link2.x1, link2.y1)

    intersectsSNode: (snode) ->
        lineRectOverlap(@x0, @y0, @x1, @y1, snode.rect)

    place: ->
        # create link div
        linkDiv = document.createElement('div')
        linkDiv.setAttribute('class', 'link')
        linkDiv.setAttribute('id', 'link' + @id)
        
        nodesDiv = document.getElementById("nodesDiv")
        nodesDiv.appendChild(linkDiv)

        $('#link' + @id).append('<div class="linkLine" id="linkLine' + @id + '"></div>')
        $('#link' + @id).append('<div class="linkLabel" id="linkLabel' + @id + '">' + @label + '</div>')

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
        tz = cz
        
        transformStr = 'translate3d(' + tx + 'px,' + ty + 'px,' + tz + 'px)'
        + ' rotateZ(' + rotz + 'rad)'
        + ' rotateY(' + roty + 'rad)';
        $('#link' + @id).css('-webkit-transform', transformStr)