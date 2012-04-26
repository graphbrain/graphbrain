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

        # eccentricity
        @negativeStretch = 5
        @mappingPower = 2

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
                @z *= @negativeStretch

    cartesianToSpherical: ->
        @r = Math.sqrt(@x * @x + @y * @y + @z * @z)
        @theta = Math.atan2(@z, @x) - (Math.PI / 2) 
        if @theta < -Math.PI
            @theta += 2 * Math.PI
        @phi = Math.acos(@y / @r) - (Math.PI / 2)

    viewMapping: ->
        if @theta > 0
            d = (Math.PI - @theta) / Math.PI
            d = d * d
            d *= Math.PI
            @theta = Math.PI - d
        else if @theta < 0
            d = (-Math.PI - @theta) / Math.PI
            d = Math.abs(Math.pow(d, @mappingPower))
            d *= -Math.PI
            @theta = -Math.PI - d

        if @phi > 0
            d = ((Math.PI / 2) - @phi) / (Math.PI / 2)
            d = d * d
            d *= (Math.PI / 2)
            @phi = (Math.PI / 2) - d
        else if @phi < 0
            d = (-(Math.PI / 2) - @phi) / (Math.PI / 2)
            d = Math.abs(Math.pow(d, @mappingPower))
            d *= -(Math.PI / 2)
            @phi = -(Math.PI / 2) - d