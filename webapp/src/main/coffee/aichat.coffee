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

aiChatAddLine = (line) ->
  $('#ai-chat-log').append(line)
  height = $('#ai-chat')[0].scrollHeight;
  $('#ai-chat').scrollTop(height);

aiChatSubmit = (msg) ->
  sentence = $('#ai-chat-input').val()
  aiChatAddLine('<br />you: ' + sentence)
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
  reply = $.parseJSON(msg)
  aiChatAddLine('<br />GraphBrain: ' + reply['sentence'])

  if reply['goto'] != ''
    window.location.href = '/node/' + reply['goto']

showAiChat = () ->
  $('#ai-chat').css('display', 'block')

hideAiChat = () ->
  $('#ai-chat').css('display', 'none')

aiChatButtonPressed = (msg) ->
  if aiChatVisible
  	aiChatVisible = false
  	hideAiChat()
  else
  	aiChatVisible = true
  	showAiChat()