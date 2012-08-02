# (c) 2012 GraphBrain Ltd. All rigths reserved.

# Entry point functions & global variables

nodeView = false

$ ->
    if (typeof snodes != 'undefined')
        nodeView = true

    if nodeView
        initGraph()
        initTextView()
    
    initInterface()