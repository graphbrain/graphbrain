aiChatVisible = false

initAiChat = () ->
	html = """
	<div id="ai-chat-log" />
	<form id="ai-chat-form">
	<input id="ai-chat-input" type="text" />
	</form>
	"""
	$('#ai-chat').html(html)
	$('#ai-chat-form').submit(aiChatSubmit)

aiChatSubmit = (msg) ->
  $('#ai-chat-log').append('<br />you said: ' + $('#ai-chat-input').val())
  $('#ai-chat-input').val('')
  false

showAiChat = () ->
  $('#ai-chat').css('visibility', 'visible')

hideAiChat = () ->
  $('#ai-chat').css('visibility', 'hidden')

aiChatButtonPressed = (msg) ->
  if aiChatVisible
  	aiChatVisible = false
  	hideAiChat()
  else
  	aiChatVisible = true
  	showAiChat()