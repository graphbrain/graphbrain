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
  sentence = $('#ai-chat-input').val()
  $('#ai-chat-log').append('<br />you: ' + sentence)
  $('#ai-chat-input').val('')
  $.ajax({
    type: "POST",
    url: "/ai",
    data: "sentence=" + sentence + "&rootId=" + rootNodeId,
    dataType: "text",
    success: aiChatReply
  })
  false

aiChatReply = (msg) ->
  $('#ai-chat-log').append('<br />GraphBrain: ' + msg)

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