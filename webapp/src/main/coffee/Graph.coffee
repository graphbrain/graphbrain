# (c) 2012 GraphBrain Ltd. All rigths reserved.

rootNodeId = false


class Graph
    constructor: (width, height, newedges) ->
        @width = width
        @height = height
        @newedges = newedges

        @halfWidth = width / 2
        @halfHeight = height / 2

        @snodes = {}
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

        # new edges
        @changedSNode = null


    @initGraph = (newedges) ->
        graph = new Graph($('#graph-view').width(), $('#graph-view').height(), newedges)
        graph.updateTransform()

        # create root
        snode = new SNode(graph, 'root', '', 0, '', '#000', true)
        graph.snodes['root'] = snode
        graph.root = snode
        nid = data['root']['id']
        rootNodeId = nid
        text = data['root']['text']
        type = data['root']['type']
    
        if type == 'url'
            node = new Node(nid, text, type, snode, '', data['root']['url'], data['root']['icon'])
        else
            node = new Node(nid, text, type, snode, '')
        
        node.root = true    
        snode.nodes[nid] = node
        graph.rootNode = node

        snode.place()

        graph.addSNodesFromJSON(data)

        graph


    addSNodesFromJSON: (json) ->
        for k, v of json['snodes']
            label = v['label']
            
            # snodes labeled 'x' should be discarded
            if (label != 'x') and (label != 'X')
                sid = k
                etype = v['etype']
                rpos = v['rpos']
                color = v['color']
                nlist = v['nodes']
        
                snode = new SNode(this, sid, etype, rpos, label, color, false)
                @snodes[sid] = snode

                for nod in nlist
                    nid = nod['id']
                    text = nod['text']
                    type = nod['type']
                    edge = nod['edge']

                    glow = false
                    if @newedges != undefined
                        for e in @newedges
                            if e != ''
                                if e == edge
                                    @changedSNode = snode
                                    glow = true

                    if type == 'url'
                        node = new Node(nid, text, type, snode, edge, nod['url'], nod['icon'], glow)
                    else
                        node = new Node(nid, text, type, snode, edge, '', '', glow)
            
                    snode.nodes[nid] = node

                snode.place()

        @layout()


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


    updateView: ->
        @snodes[k].applyPos() for k of @snodes


    layout: ->
        @snodes[k].initPosAndLayout() for k of @snodes

        # layout root node
        @root.moveTo(0, 0, 0)

        # create snode array
        snodeArray = []
        snodeArray.push(@snodes[key]) for key of @snodes when @snodes.hasOwnProperty(key) and !@snodes[key].isRoot

        layout(snodeArray)

        # calc eccentricity params
        @negativeStretch = 1
        @mappingPower = 1

        N = snodeArray.length
        Nt = 7

        if (N > (Nt * 2))
            @mappingPower = Math.log(Math.asin(Nt / (N / 2)) / Math.PI) * (1 / Math.log(0.5))
            @negativeStretch = @mappingPower * 2

        @updateView()


    label: (text, relpos) ->
        if relpos == 0
            text += ' ' + @rootNode['text']
        else
            text