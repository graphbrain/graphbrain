# (c) 2012 GraphBrain Ltd. All rigths reserved.


class Graph
    constructor: (width, height) ->
        @width = width
        @height = height
        @halfWidth = width / 2
        @halfHeight = height / 2

        @snodes = {}
        @snodeArray = []
        @nodes = {}
        @links = []
        @scale = 1
        @offsetX = 0
        @offsetY = 0
        @zOffset = 0

        # auxiliary quaternions and matrices for 3D rotation
        @quat = new Quaternion()
        @deltaQuat = new Quaternion()
        @affinMat = new Array(16)
        @quat.getMatrix(@affinMat)

    updateTransform: ->
        transformStr = "translate(" + @offsetX + "px," + @offsetY + "px)" +
            " scale(" + @scale + ")"
        $('#nodesDiv').css('-webkit-transform', transformStr)
        $('#nodesDiv').css('-moz-transform', transformStr)

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
        @updateDetailLevel()


    placeNodes: -> @snodes[key].place() for key of @snodes when @snodes.hasOwnProperty(key)


    placeLinks: -> link.place() for link in @links


    updateViewLinks: -> link.visualUpdate() for link in @links

    updateView: -> 
        for key of @snodes when @snodes.hasOwnProperty(key)
            sn = @snodes[key]
            sn.moveTo(sn.x, sn.y, sn.z)

        @updateViewLinks()

    updateDetailLevel: -> @snodes[key].updateDetailLevel(@scale) for key of @snodes when @snodes.hasOwnProperty(key)

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

    layout: ->
        # set all super nodes non-fixed
        @snodes[key].fixed = false for key of @snodes when @snodes.hasOwnProperty(key)

        # layout root node
        @root.moveTo(0, 0, 0)
        @root.fixed = true

        # create snode array
        @snodeArray.push(@snodes[key]) for key of @snodes when @snodes.hasOwnProperty(key) and !@snodes[key].fixed

        layout()

        for i in [0..(@snodeArray.length - 1)]
            @snodeArray[i].applyPos()

    layout2: ->
        coords = {
            0: [-0.7, 0, 0],
            1: [0.7, 0, 0],
            2: [0, 0.7, 0],
            3: [0, -0.7, 0],
            4: [-0.5, -0.5, -0.5],
            5: [0.5, 0.5, -0.5],
            6: [-0.5, 0.5, -0.5],
            7: [0.5, -0.5, -0.5],
            8: [-0.5, -0.5, 0.5],
            9: [0.5, 0.5, 0.5],
            10: [-0.5, 0.5, 0.5],
            11: [0.5, -0.5, 0.5]
        }

        # set all super nodes non-fixed
        @snodes[key].fixed = false for key of @snodes when @snodes.hasOwnProperty(key)

        # layout root node
        @root.moveTo(width / 2, height / 2, 0)
        @root.fixed = true

        snodeCount = Object.keys(@snodes).length

        for i in [0..(snodeCount-1)]
            snode = @nextByWeight(1)
            if !snode
                break

            x = width / 2
            y = height / 2
            z = 0

            if i < Object.keys(coords).length
                x += coords[i][0] * (width / 2)
                y += coords[i][1] * (height / 2)
                z += coords[i][2] * (height / 2)
            snode.moveTo(x, y, z)
            snode.fixed = true