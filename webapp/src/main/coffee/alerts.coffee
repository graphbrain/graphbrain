initAlert = ->
	$('#alert').css('display', 'none')
	$('#alert').css('visibility', 'visible')

setInfoAlert = (msg) ->
    #$('#alert').css('visibility', 'visible')
    $('#alert').css('display', 'block')
    $('#alert').removeClass('alert-error')
    $('#alert').addClass('alert-info')
    $('#alertMsg').html(msg)

setErrorAlert = (msg) ->
    #$('#alert').css('visibility', 'visible')
    $('#alert').css('display', 'block')
    $('#alert').removeClass('alert-info')
    $('#alert').addClass('alert-error')
    $('#alertMsg').html(msg)

hideAlert = ->
    #$('#alert').css('visibility', 'hidden')
    $('#alert').css('display', 'none')