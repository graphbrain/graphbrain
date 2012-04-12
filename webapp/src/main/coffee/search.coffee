showSearchDialog = (msg) ->
    dialogHtml = $("""
<div class="modal" id="searchResultsModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Search Results</h3>
  </div>
  <div class="modal-body">
    """ + msg + """
  </div>
  <div class="modal-footer">
    <a class="btn btn-primary" data-dismiss="modal">Close</a>
  </div>
</div>
        """)
    dialogHtml.appendTo('body')

    $('#searchResultsModal').on('hidden', () -> $('#searchResultsModal').remove())

    $('#searchResultsModal').modal()

resultsReceived = (msg) ->
  showSearchDialog(msg)

searchQuery = ->
  $.ajax({
    type: "POST",
    url: "/search",
    data: "q=" + $("#search-input-field").val(),
    success: resultsReceived
  })
  false