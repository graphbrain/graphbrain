var initSearchDialog, resultsReceived, searchQuery, searchRequest, showSearchDialog;

initSearchDialog = function() {
  var dialogHtml;
  dialogHtml = $("<div class=\"modal hide\" id=\"searchResultsModal\">\n  <div class=\"modal-header\">\n    <a class=\"close\" data-dismiss=\"modal\">×</a>\n    <h3>Search Results</h3>\n  </div>\n  <div class=\"modal-body\" id=\"searchResultsBody\" />\n  <div class=\"modal-footer\">\n    <a class=\"btn btn-primary\" data-dismiss=\"modal\">Close</a>\n  </div>\n</div>");
  dialogHtml.appendTo('body');
  return $('#searchResultsModal').modal({
    show: false
  });
};

showSearchDialog = function(msg) {
  return $('#searchResultsModal').modal('show');
};

resultsReceived = function(msg) {
  var html, json, numResults, r, results, _i, _len;
  json = JSON.parse(msg);
  html = '';
  numResults = json['count'];
  results = json['results'];
  if (numResults === '0') {
    html += '<p>Sorry, no results found.</p>';
  } else {
    html += '<p>' + numResults + ' results found.</p>';
    for (_i = 0, _len = results.length; _i < _len; _i++) {
      r = results[_i];
      html += '<p><a href="/node/' + r[0] + '">' + r[1] + '</a></p>';
    }
  }
  $('#searchResultsBody').html(html);
  return showSearchDialog(msg);
};

searchRequest = function(query, callback) {
  return $.ajax({
    type: "POST",
    url: "/search",
    data: "q=" + query.toLowerCase(),
    dataType: "text",
    success: callback
  });
};

searchQuery = function() {
  searchRequest($("#search-input-field").val(), resultsReceived);
  return false;
};
