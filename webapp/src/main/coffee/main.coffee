# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Entry point functions & global variables

g = false

$ ->
    g = Graph.initGraph()
    initTextView(g)
    initInterface()
    browserSpecificTweaks()
    initAnimation()