var aiChatAddLine, aiChatAddLineRaw, aiChatButtonPressed, aiChatGotoBottom, aiChatReply, aiChatSubmit, aiChatVisible, chatBuffer, chatBufferPos, chatBufferSize, clearChatBuffer, hideAiChat, initAiChat, initChatBuffer, printHelp, root, showAiChat;

aiChatVisible = false;

chatBuffer = [];

chatBufferPos = 0;

chatBufferSize = 100;

initChatBuffer = function() {
  var curPos, firstUse, line, pos, _i;
  firstUse = false;
  if (localStorage.getItem('chatBufferPos') !== null) {
    chatBufferPos = parseInt(localStorage.getItem('chatBufferPos'));
  } else {
    firstUse = true;
  }
  for (pos = _i = 0; 0 <= chatBufferSize ? _i <= chatBufferSize : _i >= chatBufferSize; pos = 0 <= chatBufferSize ? ++_i : --_i) {
    chatBuffer.push(localStorage.getItem('chatBuffer' + pos));
  }
  curPos = chatBufferPos;
  while (curPos < chatBufferSize) {
    line = chatBuffer[curPos];
    if (line !== null) {
      aiChatAddLineRaw(line);
    }
    curPos += 1;
  }
  curPos = 0;
  while (curPos < chatBufferPos) {
    line = chatBuffer[curPos];
    if (line !== null) {
      aiChatAddLineRaw(line);
    }
    curPos += 1;
  }
  if (firstUse) {
    return printHelp();
  }
};

clearChatBuffer = function() {
  var pos, _i, _results;
  localStorage.removeItem('chatBufferPos');
  _results = [];
  for (pos = _i = 0; 0 <= chatBufferSize ? _i <= chatBufferSize : _i >= chatBufferSize; pos = 0 <= chatBufferSize ? ++_i : --_i) {
    _results.push(localStorage.removeItem('chatBuffer' + pos));
  }
  return _results;
};

aiChatGotoBottom = function() {
  var height;
  height = $('#ai-chat')[0].scrollHeight;
  return $('#ai-chat').scrollTop(height);
};

showAiChat = function() {
  $('#ai-chat').css('display', 'block');
  aiChatVisible = true;
  localStorage.setItem('aichat', 'true');
  aiChatGotoBottom();
  return $('#ai-chat-input').focus();
};

hideAiChat = function() {
  $('#ai-chat').css('display', 'none');
  aiChatVisible = false;
  return localStorage.setItem('aichat', 'false');
};

initAiChat = function() {
  var html;
  html = "<div id=\"ai-chat-log\" />\n<form id=\"ai-chat-form\">\n<input id=\"ai-chat-input\" type=\"text\" />\n</form>";
  $('#ai-chat').html(html);
  $('#ai-chat-form').submit(aiChatSubmit);
  initChatBuffer();
  if (localStorage.getItem('aichat') === 'false') {
    return hideAiChat();
  } else {
    $("#ai-chat-button").button('toggle');
    return showAiChat();
  }
};

aiChatAddLineRaw = function(line) {
  $('#ai-chat-log').append(line);
  return aiChatGotoBottom();
};

aiChatAddLine = function(agent, line) {
  var html;
  html = '';
  if (agent === 'gb') {
    html += '<div class="gb-line"><b>GraphBrain:</b> ';
  } else if (agent === 'user') {
    html += '<div class="user-line"><b>You:</b> ';
  }
  html += line + '</div>';
  aiChatAddLineRaw(html);
  chatBuffer[chatBufferPos] = html;
  localStorage.setItem('chatBuffer' + chatBufferPos, html);
  chatBufferPos += 1;
  if (chatBufferPos >= chatBufferSize) {
    chatBufferPos = 0;
  }
  return localStorage.setItem('chatBufferPos', chatBufferPos);
};

printHelp = function() {
  var helpMsg;
  helpMsg = "GraphBrain allows you to record facts as relationships between entities (web resources, objects, concepts).<br />\nTo add a fact, simply type a sentence with a verb linking two entities (objects, concepts, websites), e.g.<br />\n\n<b>GraphBrain likes people</b><br />\n<b>GraphBrain lives at http://graphbrain.com</b><br />\n\nIn cases where there may be ambiguity, try to use quotation marks, e.g.<br />\n<b>\"Burn after reading\" is a film</b> <br />";
  return aiChatAddLine('gb', helpMsg);
};

aiChatSubmit = function(msg) {
  var sentence;
  sentence = $('#ai-chat-input').val();
  aiChatAddLine('user', sentence);
  $('#ai-chat-input').val('');
  if (sentence === '!clean') {
    clearChatBuffer();
    location.href = location.href;
    return false;
  }
  if (sentence === 'help') {
    printHelp();
    return false;
  }
  $.ajax({
    type: "POST",
    url: "/ai",
    data: "sentence=" + sentence + "&rootId=" + rootNodeId,
    dataType: "json",
    success: aiChatReply
  });
  return false;
};

aiChatReply = function(msg) {
  aiChatAddLine('gb', msg['sentence']);
  state.setNewEdges(msg['newedges']);
  if (msg['goto'] !== '') {
    return window.location.href = '/node/' + msg['goto'];
  }
};

aiChatButtonPressed = function(msg) {
  if (aiChatVisible) {
    return hideAiChat();
  } else {
    return showAiChat();
  }
};

root = typeof exports !== "undefined" && exports !== null ? exports : this;

root.aiChatDisambiguate = function(mode, text, rel, participantIds, pos) {
  return disambiguateQuery(mode, text, rel, participantIds, pos);
};