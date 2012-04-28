# (c) 2012 GraphBrain Ltd. All rigths reserved.



class SphericalCoords
    constructor: ->
        # spherical coords
        @theta = 0
        @phi = 0
        @r = 0

        # Cartesian coords
        @x = 0
        @y = 0
        @z = 0

    sphericalToCartesian: ->
        if @r == 0
            @x = 0
            @y = 0
            @z = 0
        else
            theta= @theta + (Math.PI / 2)
            phi  = @phi + (Math.PI / 2)
            @x = @r * Math.cos(theta) * Math.sin(phi)
            @y = @r * Math.cos(phi)
            @z = @r * Math.sin(theta) * Math.sin(phi)
            if @z < 0
                @z *= g.negativeStretch

    cartesianToSpherical: ->
        @r = Math.sqrt(@x * @x + @y * @y + @z * @z)
        @theta = Math.atan2(@z, @x) - (Math.PI / 2) 
        if @theta < -Math.PI
            @theta += 2 * Math.PI
        @phi = Math.acos(@y / @r) - (Math.PI / 2)

    scoordMapping: (ang, maxAng) ->
        _maxAng = maxAng
        if ang < 0
            _maxAng = -maxAng

        d = Math.abs((_maxAng - ang) / maxAng)
        d = Math.abs(Math.pow(d, g.mappingPower))
        d *= _maxAng
        _maxAng - d

    viewMapping: ->
        @theta = @scoordMapping(@theta, Math.PI)
        @phi = @scoordMapping(@phi, Math.PI / 2)