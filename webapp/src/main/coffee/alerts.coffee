setInfoAlert = (msg) ->
    $('#alert').css('visibility', 'visible')
    $('#alertMsg').html(msg)

hideAlert = ->
    $('#alert').css('visibility', 'hidden')