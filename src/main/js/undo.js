var root, undoFactReply;

undoFactReply = function(msg) {
  aiChatAddLine('gb', 'fact removed (undo).');
  return window.location.reload();
};

root = typeof exports !== "undefined" && exports !== null ? exports : this;

root.undoFact = function(rel, participants) {
  var params;
  params = "&rel=" + encodeURIComponent(rel);
  params += "&participants=" + encodeURIComponent(participants);
  return $.ajax({
    type: "POST",
    url: "/undo_fact",
    data: params,
    dataType: "text",
    success: undoFactReply
  });
};
