# (c) 2012 GraphBrain Ltd. All rigths reserved.

g = false
rootNodeId = false

initGraph = ->
    g = new Graph($('#graph-view').width(), $('#graph-view').height())
    g.updateTransform()

    # create root
    snode = new SNode('root', '', 0, '', '#000', true)
    g.snodes['root'] = snode
    g.root = snode
    nid = data['root']['id']
    rootNodeId = nid
    text = data['root']['text']
    type = data['root']['type']
    
    if type == 'url'
        node = new Node(nid, text, type, snode, data['root']['url'], data['root']['icon'])
    else
        node = new Node(nid, text, type, snode)
            
    snode.nodes[nid] = node
    g.nodes[nid] = node
    g.rootNode = node

    # process super nodes and associated nodes
    for k, v of data['snodes']
        sid = k
        etype = v['etype']
        label = v['label']
        rpos = v['rpos']
        color = v['color']
        nlist = v['nodes']
        
        snode = new SNode(sid, etype, rpos, label, color, false)
        g.snodes[sid] = snode

        for nod in nlist
            nid = nod['id']
            text = nod['text']
            type = nod['type']

            if type == 'url'
                node = new Node(nid, text, type, snode, nod['url'], nod['icon'])
            else
                node = new Node(nid, text, type, snode)
            
            snode.nodes[nid] = node
            g.nodes[nid] = node

    g.genSNodeKeys()

    g.placeNodes()
    g.layout()
    g.updateView()


class Graph
    constructor: (width, height) ->
        @width = width
        @height = height
        @halfWidth = width / 2
        @halfHeight = height / 2

        @snodes = {}
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

    updateSize: ->
        @width = $('#graph-view').width()
        @height = $('#graph-view').height()
        @halfWidth = @width / 2
        @halfHeight = @height / 2

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


    placeNodes: -> g.snodes[key].place() for key of g.snodes when g.snodes.hasOwnProperty(key)


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
        @snodes[key].initLayout() for key of @snodes when @snodes.hasOwnProperty(key)

        # layout root node
        @root.moveTo(0, 0, 0)
        @root.fixed = true

        # create snode array
        @snodeArray = []
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

        if N > 0
          for i in [0..(@snodeArray.length - 1)]
            @snodeArray[i].applyPos()
