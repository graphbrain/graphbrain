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
  $('#searchResultsBody').html(msg)
  showSearchDialog(msg)

searchQuery = ->
  $.ajax({
    type: "POST",
    url: "/search",
    data: "q=" + $("#search-input-field").val(),
    dataType: "text",
    success: resultsReceived
  })
  false