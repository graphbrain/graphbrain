undoFactReply = (msg) ->
  aiChatAddLine('<br />GraphBrain: fact removed (undo).')
  window.location.reload()

root = exports ? this
root.undoFact = (rel, participants) ->
  params = "&rel=" + encodeURIComponent(rel)
  params += "&participants=" + encodeURIComponent(participants)
  $.ajax({
    type: "POST",
    url: "/undo_fact",
    data: params
    dataType: "text",
    success: undoFactReply
  })