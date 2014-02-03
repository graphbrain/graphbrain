var disambiguateActionReply, disambiguateQuery, disambiguateResultsReceived, hideDisambiguateDialog, initDisambiguateDialog, root, showDisambiguateDialog;

initDisambiguateDialog = function() {
  var dialogHtml;
  dialogHtml = $("<div class=\"modal hide\" id=\"disambiguateModal\">\n  <div class=\"modal-header\">\n    <a class=\"close\" data-dismiss=\"modal\">×</a>\n    <h3>Did you mean...</h3>\n  </div>\n  <div class=\"modal-body\" id=\"disambiguateBody\" />\n  <div class=\"modal-footer\">\n    <a class=\"btn btn-primary\" data-dismiss=\"modal\">Close</a>\n  </div>\n</div>");
  dialogHtml.appendTo('body');
  return $('#disambiguateModal').modal({
    show: false
  });
};

showDisambiguateDialog = function(msg) {
  return $('#disambiguateModal').modal('show');
};

hideDisambiguateDialog = function(msg) {
  return $('#disambiguateModal').modal('hide');
};

disambiguateResultsReceived = function(msg) {
  var html, json, mode, participants, pos, r, rel, results, text, _i, _len;
  json = JSON.parse(msg);
  mode = json['mode'];
  text = json['text'];
  rel = json['rel'];
  participants = json['participants'];
  pos = json['pos'];
  results = json['results'];
  html = '<p><a href="#" onclick="disambiguateCreateNode(\'' + mode + "','" + text + "','" + rel + "','" + participants + "'," + pos + ')">Create new</a></p>';
  for (_i = 0, _len = results.length; _i < _len; _i++) {
    r = results[_i];
    html += '<p><a href="#" onclick="disambiguateChangeNode(\'' + mode + "','" + rel + "','" + participants + "'," + pos + ",'" + r[0] + '\')">' + r[1] + '</a></p>';
  }
  $('#disambiguateBody').html(html);
  return showDisambiguateDialog(msg);
};

disambiguateQuery = function(mode, text, rel, participantIds, pos) {
  var params, participants;
  participants = participantIds.join(" ");
  params = "text=" + text;
  params += "&mode=" + mode;
  params += "&rel=" + encodeURIComponent(rel);
  params += "&participants=" + encodeURIComponent(participants);
  params += "&pos=" + pos;
  return $.ajax({
    type: "POST",
    url: "/disambig",
    data: params,
    dataType: "text",
    success: disambiguateResultsReceived
  });
};

disambiguateActionReply = function(msg) {
  aiChatAddLine('gb', 'fact updated.');
  return window.location.reload();
};

root = typeof exports !== "undefined" && exports !== null ? exports : this;

root.disambiguateCreateNode = function(mode, text, rel, participants, pos) {
  var params;
  params = "&mode=" + mode;
  params += "&text=" + encodeURIComponent(text);
  params += "&rel=" + encodeURIComponent(rel);
  params += "&participants=" + encodeURIComponent(participants);
  params += "&pos=" + pos;
  $.ajax({
    type: "POST",
    url: "/disambig_create",
    data: params,
    dataType: "text",
    success: disambiguateActionReply
  });
  return hideDisambiguateDialog();
};

root.disambiguateChangeNode = function(mode, rel, participants, pos, changeTo) {
  var params;
  params = "&mode=" + mode;
  params += "&rel=" + encodeURIComponent(rel);
  params += "&participants=" + encodeURIComponent(participants);
  params += "&pos=" + pos;
  params += "&changeto=" + encodeURIComponent(changeTo);
  $.ajax({
    type: "POST",
    url: "/disambig_change",
    data: params,
    dataType: "text",
    success: disambiguateActionReply
  });
  return hideDisambiguateDialog();
};
