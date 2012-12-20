# Graph interface
dragging = false
lastX = 0
lastY = 0
lastScale = -1
scroll = false

scrollOn = (e) ->
    scroll = true

scrollOff = (e) ->
    scroll = false

mouseUp = (e) ->
    dragging = false
    false

mouseDown = (e) ->
    dragging = true
    lastX = e.pageX
    lastY = e.pageY
    stopAnim = true
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

touchStart = (e) ->
    stopAnim = true
    if e.touches.length == 1
        touch = e.touches[0]
        lastX = touch.pageX
        lastY = touch.pageY
    true

touchEnd = (e) ->
    lastScale = -1
    true

touchMove = (e) ->
    if e.touches.length == 1
        e.preventDefault()
        touch = e.touches[0]
        deltaX = touch.pageX - lastX
        deltaY = touch.pageY - lastY
        lastX = touch.pageX
        lastY = touch.pageY
        g.rotateX(-deltaX * 0.0015)
        g.rotateY(deltaY * 0.0015)
        g.updateView()
        false
    else if e.touches.length == 2
        e.preventDefault()
        dx = e.touches[0].pageX - e.touches[1].pageX
        dy = e.touches[0].pageY - e.touches[1].pageY
        scale = Math.sqrt(dx * dx + dy * dy)
        if lastScale >= 0
            x = (e.touches[0].pageX + e.touches[1].pageX) / 2
            y = (e.touches[0].pageY + e.touches[1].pageY) / 2
            deltaScale = (scale - lastScale) * 0.025
            g.zoom(deltaScale, x, y)
        lastScale = scale
        false
    true

mouseWheel = (e, delta, deltaX, deltaY) ->
    if (!scroll)
        g.zoom(deltaY, e.pageX, e.pageY)
    true

fullBind = (eventName, f) ->
    $("#graph-view").bind eventName, f
    $(".snode1").bind eventName, f
    $(".snodeN").bind eventName, f
    $(".link").bind eventName, f

# init
initInterface = ->
    $('#search-field').submit(searchQuery)
    initSearchDialog()
    initSignUpDialog()
    initLoginDialog()
    $('.signupLink').bind 'click', showSignUpDialog
    $('#loginLink').bind 'click', showLoginDialog
    $('#logoutLink').bind 'click', logout

    fullBind("mouseup", mouseUp)
    fullBind("mousedown", mouseDown)
    fullBind("mousemove", mouseMove)
    fullBind("mousewheel", mouseWheel)
    
    document.addEventListener('touchstart', touchStart)
    document.addEventListener('touchend', touchEnd)
    document.addEventListener('touchmove', touchMove)

    initAlert()
    initAiChat()
    initRemoveDialog()
    initDisambiguateDialog()
    $('#ai-chat-button').bind 'click', aiChatButtonPressed
    $('#removeButton').bind 'click', removeButtonPressed

    # display error message if defined
    if errorMsg != ''
        setErrorAlert(errorMsg)