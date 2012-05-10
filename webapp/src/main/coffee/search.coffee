initSearchDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="searchResultsModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Search Results</h3>
  </div>
  <div class="modal-body" id="searchResultsBody" />
  <div class="modal-footer">
    <a class="btn btn-primary" data-dismiss="modal">Close</a>
  </div>
</div>
    """)
    dialogHtml.appendTo('body')
    $('#searchResultsModal').modal({show: false})

showSearchDialog = (msg) ->
  $('#searchResultsModal').modal('show')

resultsReceived = (msg) ->
  json = JSON.parse(msg)
  html = '<div>'
  numResults = json['count']
  results = json['results']
  if numResults == '0'
    html += '<p>Sorry, no results found.</p>'
  else
    html += '<p>' + numResults + ' results found.</p>'
    for r in results
      html += '<p><a href="/node/' + r[0] + '">' + r[1] + '</a></p>'
  html += '</div>'
  $('#searchResultsBody').html(html)
  showSearchDialog(msg)

searchRequest = (query, callback) ->
  $.ajax({
    type: "POST",
    url: "/search",
    data: "q=" + query.toLowerCase(),
    dataType: "text",
    success: callback
  })

searchQuery = ->
  searchRequest($("#search-input-field").val(), resultsReceived)
  false