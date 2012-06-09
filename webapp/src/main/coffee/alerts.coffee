setInfoAlert = (msg) ->
    $('#alert').css('visibility', 'visible')
    $('#alert').removeClass('alert-error')
    $('#alert').addClass('alert-info')
    $('#alertMsg').html(msg)

setErrorAlert = (msg) ->
    $('#alert').css('visibility', 'visible')
    $('#alert').removeClass('alert-info')
    $('#alert').addClass('alert-error')
    $('#alertMsg').html(msg)

hideAlert = ->
    $('#alert').css('visibility', 'hidden')