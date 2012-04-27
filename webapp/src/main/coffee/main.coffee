# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Entry point functions & global variables
g = false

initGraph = ->
    g = new Graph(window.innerWidth, window.innerHeight)
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
            starg  = g.snodes[l['starg']]
        link = new Link(linkID++, false, sorig, false, starg, l['relation'])
        g.links.push(link)
        sorig.links.push(link)
        starg.links.push(link)

    g.placeNodes()
    g.placeLinks()
    g.layout()
    g.updateView()


$ ->
    initGraph()
    initInterface()