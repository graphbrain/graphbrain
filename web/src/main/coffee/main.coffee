# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Entry point functions & global variables

g = false
state = false

$ ->
    Math.seedrandom("GraphBrain GraphBrain")

    state = new State()

    if data?
        g = Graph.initGraph(state.getNewEdges())
    
    initInterface()
    
    if data?
        initRelations()
    
    browserSpecificTweaks()

    if data?
        if g.changedSNode == null
            addAnim(new AnimInitRotation())
        else
            addAnim(new AnimLookAt(g.changedSNode))

    state.clean()