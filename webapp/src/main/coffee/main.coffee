# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Entry point functions & global variables

nodeView = false

$ ->
    initGraph()
    initTextView()
    initInterface()
    browserSpecificTweaks()
    initAnimation()