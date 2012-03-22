# (c) 2012 GraphBrain Ltd. All rigths reserved.

# GraphPos is an auxliary class used to layout snodes
class GraphPos
    constructor: (@snode, @width, @height) ->
        @angDivs = 12
        @radDivs = 10
        @ang2 = Math.PI * 0.5

        @halfWidth = @width / 2
        @halfHeight = @height / 2

        @done = false
        @angStep = 0
        @radStep = 1
        @x = @halfWidth
        @y = @halfHeight

        if @snode.depth > 1
            deltaX = @snode.parent.x - @halfWidth
            deltaY = @snode.parent.y - @halfHeight
            @baseAngle = Math.atan2(deltaY, deltaX)
            @minRadius = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY))
            @maxRadius = Math.sqrt((@halfWidth * @halfWidth) + (@halfHeight * @halfHeight))

        @next()


    next: ->
        if @snode.depth == 1
            @next1()
        else
            @next2()


    next1: ->
        if @angStep >= @angDivs
            @radStep++
            @angStep = 0

        if @radStep > @radDivs
            @done = true
            return

        angle = Math.PI * 2 * (@angStep / @angDivs)
        a = @halfWidth * (@radStep / @radDivs)
        b = @halfHeight * (@radStep / @radDivs)

        @x = @halfWidth + (a * Math.cos(angle))
        @y = @halfHeight + (b * Math.sin(angle))

        @angStep++


    next2: ->
        if @angStep >= @angDivs
            @radStep++
            @angStep = 0

        if @radStep >= @radDivs
            @done = true
            return

        angle = (@ang2 * (@angStep / @angDivs)) - (@ang2 / 2)
        angle += @baseAngle

        r = @radStep / @radDivs
        r *= @maxRadius - @minRadius
        r += @minRadius

        @x = @halfWidth + (r * Math.cos(angle))
        @y = @halfHeight + (r * Math.sin(angle))

        @angStep++


# Graph
class Graph
    constructor: ->
        @snodes = {}
        @nodes = {}
        @links = []
        @newNode = false
        @newNodeActive = false

        # auxiliary quaternions and matrices for 3D rotation
        @quat = new Quaternion()
        @deltaQuat = new Quaternion()
        @affinMat = new Array(16)
        @quat.getMatrix(@affinMat)


    rotateX: (angle) ->
        @deltaQuat.fromEuler(angle, 0, 0)
        @quat.mul(@deltaQuat)
        @quat.normalise()
        @quat.getMatrix(@affinMat)


    rotateY: (angle) ->
        @deltaQuat.fromEuler(0, 0, angle)
        @quat.mul(@deltaQuat)
        @quat.normalise()
        @quat.getMatrix(@affinMat)


    placeNodes: -> @snodes[key].place() for key of @snodes when @snodes.hasOwnProperty(key)


    placeLinks: -> link.place() for link in @links


    updateViewLinks: -> link.visualUpdate() for link in @links


    updateView: -> 
        for key of @snodes when @snodes.hasOwnProperty(key)
            sn = @snodes[key]
            sn.moveTo(sn.x, sn.y, sn.z)

        @updateViewLinks()


    labelAtPoint: (x, y) ->
        p = [x, y]
        
        i = @links.length - 1
        while i >= 0
            if @links[i].pointInLabel(p) then return @links[i]
            i--

        return -1;


    genSNodeKeys: -> key for key in @snodes


    layoutSNode: (snode, fixedSNodes, width, height) ->
        iters = 100

        snode.fixed = true

        bestPenalty = 99999999
        bestX = bestY = 0
        
        gp = new GraphPos(snode, width, height)

        while not gp.done
            penalty = 0

            x = gp.x
            y = gp.y
            snode.updatePos(x, y)

            for snode2 in fixedSNodes
                # node - node overlap penalty
                if snode.overlaps(snode2) then penalty += 1000000

                # label - node overlap penalty
                penalty += 10000 for link in snode.links when link.overlaps(snode2)

                # link-node intersection penalty
                #for (var k = 0; k < snode.links.length; k++) {
                #    var link = snode.links[k];
                #    if (link.sorig.fixed && link.starg.fixed)
                #        if (link.intersectsSNode(snode2))
                #            penalty += 10000;
                #}

            # node-label overlap penalty
            penalty += 10000 for link in @links when link.sorig.fixed and link.starg.fixed and snode.overlaps(link)

            # link-link intersection penalty
            for link in @links when link.sorig.fixed and link.starg.fixed
                penalty += 10000 for slink in snode.links when slink.sorig.fixed and slink.starg.fixed and link.intersectsLink(slink)

            # link length penalty
            penalty += link.len for link in snode.links when link.sorig.fixed and link.starg.fixed

            #console.log("p: " + penalty + "; count:" + snode.links.length)

            if penalty < bestPenalty
                bestPenalty = penalty
                bestX = x
                bestY = y

            gp.next()

        #console.log("best: " + bestPenalty)

        snode.moveTo(bestX, bestY, 0)


    nextByWeight: (depth) ->
        bestWeight = -1
        bestSNode = false
        
        for key of @snodes when @snodes.hasOwnProperty(key)
            snode = @snodes[key]
            if (not snode.fixed) and (snode.depth == depth)
                if snode.weight > bestWeight
                    bestWeight = snode.weight
                    bestSNode = snode

        bestSNode

    signal: (value) ->
        if value >= 0
            1.0
        else
            -1.0

    layout: (width, height) ->
        coords = {
            0: [-0.7, 0, 0],
            1: [0.7, 0, 0],
            2: [0, 0.7, 0],
            3: [0, -0.7, 0],
            4: [-0.5, -0.5, -0.5],
            5: [0.5, 0.5, -0.5],
            6: [-0.5, 0.5, -0.5],
            7: [0.5, -0.5, -0.5]
        }

        @halfWidth = width / 2
        @halfHeight = height / 2

        # set all super nodes non-fixed
        @snodes[key].fixed = false for key of @snodes when @snodes.hasOwnProperty(key)

        # layout root node
        g.root.moveTo(width / 2, height / 2, 0)
        g.root.fixed = true

        snodeCount = @snodes.size()

        for i in [0..(snodeCount-1)]
            snode = @nextByWeight(1)
            if !snode
                break

            x = width / 2
            y = height / 2
            z = 0

            if i < coords.size()
                x += coords[i][0] * (width / 2)
                y += coords[i][1] * (height / 2)
                z += coords[i][2] * (height / 2)
            snode.moveTo(x, y, z)
            snode.fixed = true