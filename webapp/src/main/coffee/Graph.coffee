# (c) 2012 GraphBrain Ltd. All rigths reserved.

g = false

initGraph = ->
    console.log('width: ' + $('#graph-view').width() + '; height: ' + $('#graph-view').height())
    g = new Graph($('#graph-view').width(), $('#graph-view').height())
    g.updateTransform()

    # process super nodes and associated nodes
    for sn in snodes
        sid = sn['id']
        nlist = sn['nodes']
        
        snode = new SNode(sid)

        for nid in nlist
            nod = nodes[nid]
            text = nod['text']
            type = nod['type']
            parentID = nod['parent']
            node = false
            if type == 'url'
                node = new Node(nid, text, type, snode, nod['url'])
            else
                node = new Node(nid, text, type, snode)
            snode.nodes[nid] = node
            g.nodes[nid] = node

            if (snode.parent == 'unknown') or (parentID == '')
                snode.parent = parentID

        g.snodes[sid] = snode

    # assign root, parents and subNodes
    for sn in snodes
        sid = sn['id']
        snode = g.snodes[sid]
        parentID = snode.parent
        if parentID == ''
            g.root = snode
            snode.parent = false
        else
            snode.parent = g.nodes[parentID].snode
            g.nodes[parentID].snode.subNodes[g.nodes[parentID].snode.subNodes.length] = snode


    # assign depth and weight
    for key of g.snodes when g.snodes.hasOwnProperty(key)
        snode = g.snodes[key]
        snode.weight = Object.keys(snode.nodes).length
        if not snode.parent
            snode.depth = 0
        else if snode.parent == g.root
            snode.depth = 1
            snode.weight += subNode.nodes.size() for subNode in snode.subNodes
        else
            snode.depth = 2

    g.genSNodeKeys()

    # process links
    linkID = 0
    for l in links
        orig = ''
        targ = ''
        sorig = ''
        starg = ''
        if 'orig' of l
            orig  = g.nodes[l['orig']]
            sorig = orig.snode
        else
            orig = false
            sorig = g.snodes[l['sorig']]
        if 'targ' of l
            targ  = g.nodes[l['targ']]
            starg = targ.snode
        else
            targ = false
            starg = g.snodes[l['starg']]
        link = new Link(linkID++, false, sorig, false, starg, l['relation'], l['color'])
        g.links.push(link)
        sorig.links.push(link)
        starg.links.push(link)

        if sorig.parent == false
            starg.linkLabel = l['relation']
            starg.linkDirection = 'in'
        else
            sorig.linkLabel = l['relation']
            sorig.linkDirection = 'out'

    g.placeNodes()
    g.placeLinks()
    g.layout()
    g.updateView()


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

        # view eccentricity
        @negativeStretch = 1
        @mappingPower = 1

    updateTransform: ->
        transformStr = "translate(" + @offsetX + "px," + @offsetY + "px)" +
            " scale(" + @scale + ")"
        $('#graph-view').css('-webkit-transform', transformStr)
        $('#graph-view').css('-moz-transform', transformStr)

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


    updateViewLinks: ->
        link.updatePos() for link in @links
        link.visualUpdate() for link in @links

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

        # calc eccentricity params
        @negativeStretch = 1
        @mappingPower = 1

        N = @snodeArray.length
        Nt = 7

        if (N > (Nt * 2))
            @mappingPower = Math.log(Math.asin(Nt / (N / 2)) / Math.PI) * (1 / Math.log(0.5))
            @negativeStretch = @mappingPower * 2
            #console.log('N: ' + N)
            #console.log('Nt: ' + Nt)
            #console.log('mappingPower: ' + @mappingPower)
            #console.log('negativeStretch: ' + @negativeStretch)

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