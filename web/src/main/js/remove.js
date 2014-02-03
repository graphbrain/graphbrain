var initRemoveDialog, removeAction, removeClicked, showRemoveDialog;

removeClicked = function(msg) {
  return showRemoveDialog(msg.data.node, msg.data.link, msg.data.edge);
};

initRemoveDialog = function() {
  var dialogHtml;
  dialogHtml = $("<div class=\"modal hide\" id=\"removeModal\">\n  <div class=\"modal-header\">\n    <a class=\"close\" data-dismiss=\"modal\">×</a>\n    <h3>Confirm Removal</h3>\n  </div>\n  <form id=\"removeForm\" action='/node/" + rootNodeId + "' method=\"post\">\n  <input type=\"hidden\" name=\"op\" value=\"remove\">\n  <input id=\"removeEdgeField\" type=\"hidden\" name=\"edge\">\n  <div class=\"modal-body\" id=\"addBrainBody\">\n      <div id=\"linkDesc\"></div>\n  </div>\n  <div class=\"modal-footer\">\n    <a class=\"btn\" data-dismiss=\"modal\">Close</a>\n    <a id=\"removeDlgButton\" class=\"btn btn-primary\">Remove</a>\n  </div>\n</form>\n</div>");
  dialogHtml.appendTo('body');
  return $('#removeDlgButton').click(removeAction);
};

showRemoveDialog = function(node, link, edge) {
  $('#removeEdgeField').val(edge);
  $('#linkDesc').html(node.text + ' <strong>(' + link + '</strong>)');
  return $('#removeModal').modal('show');
};

removeAction = function() {
  return $('#removeForm').submit();
};
