# Graph interface
dragging = false
lastX = 0
lastY = 0


mouseUp = (e) ->
    dragging = false
    false

mouseDown = (e) ->
    dragging = true
    lastX = e.pageX
    lastY = e.pageY
    false

mouseMove = (e) ->
    if dragging
        deltaX = e.pageX - lastX
        deltaY = e.pageY - lastY
        lastX = e.pageX
        lastY = e.pageY
        g.rotateX(-deltaX * 0.0015)
        g.rotateY(deltaY * 0.0015)
        g.updateView()
        g.updateDetailLevel()
    false

mouseWheel = (e, delta, deltaX, deltaY) ->
    g.zoom(deltaY, e.pageX, e.pageY)

fullBind = (eventName, f) ->
    $("#graphDiv").bind eventName, f
    $(".snode1").bind eventName, f
    $(".snodeN").bind eventName, f
    $(".link").bind eventName, f

# init
initInterface = ->
    fullBind("mouseup", mouseUp)
    fullBind("mousedown", mouseDown)
    fullBind("mousemove", mouseMove)
    fullBind("mousewheel", mouseWheel)

    $('#search-field').submit(searchQuery)
    initSearchDialog()