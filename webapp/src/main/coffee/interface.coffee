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
        false
    else
        true

mouseWheel = (event, delta, deltaX, deltaY) ->
    console.log(delta, deltaX, deltaY)
    scale = g.scale
    scale += deltaY * 0.3
    if scale < 0.1
        scale = 0.1
    g.setScale(scale)

fullBind = (eventName, f) ->
    $("#overlay").bind eventName, f
    $(".snode_0").bind eventName, f
    $(".snode_1").bind eventName, f
    $(".snode1_0").bind eventName, f
    $(".snode1_1").bind eventName, f
    $(".link").bind eventName, f

initInterface = ->
    $("#overlay").bind "mouseup", mouseUp
    $(".snode_0").bind "mouseup", mouseUp
    $(".snode_1").bind "mouseup", mouseUp
    $(".snode1_0").bind "mouseup", mouseUp
    $(".snode1_1").bind "mouseup", mouseUp
    $(".link").bind "mouseup", mouseUp

    $("#overlay").bind "mousedown", mouseDown
    $(".snode_0").bind "mousedown", mouseDown
    $(".snode_1").bind "mousedown", mouseDown
    $(".snode1_0").bind "mousedown", mouseDown
    $(".snode1_1").bind "mousedown", mouseDown
    $(".link").bind "mousedown", mouseDown

    $("#overlay").bind "mousemove", mouseMove
    $(".snode_0").bind "mousemove", mouseMove
    $(".snode_1").bind "mousemove", mouseMove
    $(".snode1_0").bind "mousemove", mouseMove
    $(".snode1_1").bind "mousemove", mouseMove
    $(".link").bind "mousemove", mouseMove

    fullBind("mouseup", mouseUp)
    fullBind("mousedown", mouseDown)
    fullBind("mousemove", mouseMove)
    fullBind("mousewheel", mouseWheel)