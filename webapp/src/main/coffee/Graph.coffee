# (c) 2012 GraphBrain Ltd. All rigths reserved.

class Graph
    constructor: ->
        @snodes = {}
        @nodes = {}
        @links = []
        @scale = 0.4
        @offsetX = 0
        @offsetY = 0
        @zOffset = 300

        # auxiliary quaternions and matrices for 3D rotation
        @quat = new Quaternion()
        @deltaQuat = new Quaternion()
        @affinMat = new Array(16)
        @quat.getMatrix(@affinMat)

    updateTransform: ->
        transformStr = "translate(" + @offsetX + "px," + @offsetY + "px)" +
            " scale(" + @scale + "," + @scale + ")"
        #transformStr = "scale(" + @scale + "," + @scale + ")" +
        #    " translate(" + @offsetX + "px," + @offsetY + "px)"
        $('#nodesDiv').css('-webkit-transform', transformStr)

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

    zoom: (deltaZoom, x, y) ->
        newScale = @scale + (0.3 * deltaZoom) 
        if newScale < 0.4
            newScale = 0.4

        if deltaZoom >= 0
            rx = x - @halfWidth
            @offsetX = rx - (((rx - @offsetX) / @scale) * newScale)
            ry = y - @halfHeight
            @offsetY = ry - (((ry - @offsetY) / @scale) * newScale)
        else
            if (@scale - 0.4) > 0
                r = (newScale - 0.4) / (@scale - 0.4)
                @offsetX *= r
                @offsetY *= r

        @scale = newScale

        @updateTransform()


    placeNodes: -> @snodes[key].place() for key of @snodes when @snodes.hasOwnProperty(key)


    placeLinks: -> link.place() for link in @links


    updateViewLinks: -> link.visualUpdate() for link in @links


    updateView: -> 
        for key of @snodes when @snodes.hasOwnProperty(key)
            sn = @snodes[key]
            sn.moveTo(sn.x, sn.y, sn.z)

        @updateViewLinks()


    genSNodeKeys: -> key for key in @snodes

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