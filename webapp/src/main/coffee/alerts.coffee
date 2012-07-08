initAlert = ->
	$('#alert').css('display', 'none')
	$('#alert').css('visibility', 'visible')

setInfoAlert = (msg) ->
    $('#alert').css('display', 'block')
    $('#alert').removeClass('alert-error')
    $('#alert').addClass('alert-info')
    $('#alertMsg').html(msg)

setErrorAlert = (msg) ->
    $('#alert').css('display', 'block')
    $('#alert').removeClass('alert-info')
    $('#alert').addClass('alert-error')
    $('#alertMsg').html(msg)

hideAlert = ->
    $('#alert').css('display', 'none')