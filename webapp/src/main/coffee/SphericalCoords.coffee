# (c) 2012 GraphBrain Ltd. All rigths reserved.


normalizeAngle = (ang) ->
    while ang > Math.PI
    	ang -= (Math.PI * 2)
    while ang <= -Math.PI
    	ang += (Math.PI * 2)
    ang


angleDiff = (a1, a2) ->
	diff = Math.atan2(Math.sin(a1 - a2), Math.cos(a1 - a2))
	#console.log("a1: " + a1 + "; a2: " + a2 + "; diff: " + diff)
	diff


class SphericalCoords
    constructor: ->
    	# spherical coords
        @theta = 0
        @phi = 0
        @r = 0

        # Cartesian coords
        @scx = 0
        @scy = 0
        @scz = 0

        # velocities
        @vtheta = 0
        @vphi = 0

    sphericalToCartesian: ->
    	@scx = @r * Math.cos(@theta) * Math.sin(@phi)
    	@scy = @r * Math.sin(@theta) * Math.sin(@phi)
    	@scz = @r * Math.cos(@phi)

    cartesianToSpherical: ->
    	@theta = Math.atan2(@scy, @scx)
    	@phi = Math.acos(@scz / @r)
    	@r = Math.sqrt(@scx * @scx + @scy * @scy + @scz * @scz)
    	@normalize()

    randomSpherical: ->
    	@theta = (Math.random() * Math.PI * 2) - Math.PI
    	@phi = (Math.random() * Math.PI * 2) - Math.PI

    normalize: ->
    	@theta = normalizeAngle(@theta)
    	@phi = normalizeAngle(@phi)

    repulsion: (other, strength) ->
    	dtheta = angleDiff(@theta, other.theta)
    	dphi = angleDiff(@phi, other.theta) 
    	ang = Math.atan2(dphi, dtheta)
    	f = (1 / (dtheta * dtheta + dphi * dphi)) * strength

    	@vtheta -= f * Math.sin(ang)
    	@vphi -= f * Math.cos(ang)

   	simulationStep: (drag) ->
   		@theta += @vtheta
   		@phi += @vphi
   		@normalize()
   		@vtheta *= (1 - drag)
   		@vphi *= (1 - drag)