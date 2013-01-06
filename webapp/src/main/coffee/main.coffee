# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Entry point functions & global variables

g = false
state = false

$ ->
    Math.seedrandom("GraphBrain")

    state = new State()

    g = Graph.initGraph(state.getNewEdges())
    initInterface()
    initRelations()
    browserSpecificTweaks()

    if g.changedSNode == null
      addAnim(new AnimInitRotation())
    else
      addAnim(new AnimLookAt(g.changedSNode))

    state.clean()