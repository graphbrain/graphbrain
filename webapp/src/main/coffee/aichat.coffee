aiChatVisible = false

chatBuffer = []
chatBufferPos = 0
chatBufferSize = 100

initChatBuffer = () ->
  if localStorage.getItem('chatBufferPos') != null
    chatBufferPos = parseInt(localStorage.getItem('chatBufferPos'))

  for pos in [0..chatBufferSize]
    chatBuffer.push(localStorage.getItem('chatBuffer' + pos))

  curPos = chatBufferPos
  while curPos < chatBufferSize
    line = chatBuffer[curPos]
    if line != null
      aiChatAddLineRaw(line)
    curPos += 1
  curPos = 0
  while curPos < chatBufferPos
    line = chatBuffer[curPos]
    if line != null
      aiChatAddLineRaw(line)
    curPos += 1

aiChatGotoBottom = () ->
  height = $('#ai-chat')[0].scrollHeight
  $('#ai-chat').scrollTop(height)

showAiChat = () ->
  $('#ai-chat').css('display', 'block')
  $('#graph-view').css('height', '70%')
  $('#right-bar').css('height', '70%')
  g.updateSize()
  g.layout()
  g.updateView()
  aiChatVisible = true
  localStorage.setItem('aichat', 'true')
  aiChatGotoBottom()

hideAiChat = () ->
  $('#ai-chat').css('display', 'none')
  $('#graph-view').css('height', '100%')
  $('#right-bar').css('height', '100%')
  g.updateSize()
  g.layout()
  g.updateView()
  aiChatVisible = false
  localStorage.setItem('aichat', 'false')

initAiChat = () ->
  html = """
  <div id="ai-chat-log" />
  <form id="ai-chat-form">
  <input id="ai-chat-input" type="text" />
  </form>
  """
  $('#ai-chat').html(html)
  $('#ai-chat-form').submit(aiChatSubmit)

  initChatBuffer()

  if localStorage.getItem('aichat') == 'true'
    showAiChat()
  else
    hideAiChat()

aiChatAddLineRaw = (line) ->
  $('#ai-chat-log').append(line)
  aiChatGotoBottom()

aiChatAddLine = (line) ->
  aiChatAddLineRaw(line)
  chatBuffer[chatBufferPos] = line
  localStorage.setItem('chatBuffer' + chatBufferPos, line)
  chatBufferPos += 1
  if chatBufferPos >= chatBufferSize
    chatBufferPos = 0
  localStorage.setItem('chatBufferPos', chatBufferPos)

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

aiChatButtonPressed = (msg) ->
  if aiChatVisible
    hideAiChat()
  else
    showAiChat()