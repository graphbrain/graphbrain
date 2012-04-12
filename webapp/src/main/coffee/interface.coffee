# Graph interface
dragging = false
lastX = 0
lastY = 0


mouseUp = (e) ->
    dragging = false

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
    else
        true

mouseWheel = (e, delta, deltaX, deltaY) ->
    g.zoom(deltaY, e.pageX, e.pageY)

fullBind = (eventName, f) ->
    $("#overlay").bind eventName, f
    $(".snode1").bind eventName, f
    $(".snodeN").bind eventName, f
    $(".link").bind eventName, f

# init
initInterface = ->
    $("#overlay").bind "mouseup", mouseUp
    $(".snode1").bind "mouseup", mouseUp
    $(".snodeN").bind "mouseup", mouseUp
    $(".link").bind "mouseup", mouseUp

    $("#overlay").bind "mousedown", mouseDown
    $(".snode1").bind "mousedown", mouseDown
    $(".snodeN").bind "mousedown", mouseDown
    $(".link").bind "mousedown", mouseDown

    $("#overlay").bind "mousemove", mouseMove
    $(".snode1").bind "mousemove", mouseMove
    $(".snodeN").bind "mousemove", mouseMove
    $(".link").bind "mousemove", mouseMove

    fullBind("mouseup", mouseUp)
    fullBind("mousedown", mouseDown)
    fullBind("mousemove", mouseMove)
    fullBind("mousewheel", mouseWheel)

    $('#search-field').submit(searchQuery)