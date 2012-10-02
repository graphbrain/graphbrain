initDisambiguateDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="disambiguateModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Did you mean...</h3>
  </div>
  <div class="modal-body" id="disambiguateBody" />
  <div class="modal-footer">
    <a class="btn btn-primary" data-dismiss="modal">Close</a>
  </div>
</div>
    """)
    dialogHtml.appendTo('body')
    $('#disambiguateModal').modal({show: false})

showDisambiguateDialog = (msg) ->
  $('#disambiguateModal').modal('show')

hideDisambiguateDialog = (msg) ->
  $('#disambiguateModal').modal('hide')

disambiguateResultsReceived = (msg) ->
  json = JSON.parse(msg)

  mode = json['mode']
  text = json['text']
  rel = json['rel']
  participants = json['participants']
  pos = json['pos']
  results = json['results']

  html = '<p><a href="#" onclick="disambiguateCreateNode(\'' + mode + "','" + text + "','" + rel + "','" + participants + "'," + pos + ')">Create new</a></p>'
  for r in results
    html += '<p><a href="#" onclick="disambiguateChangeNode(\'' + mode + "','" + rel + "','" + participants + "'," + pos + ",'" + r[0] + '\')">' + r[1] + '</a></p>'
  $('#disambiguateBody').html(html)
  showDisambiguateDialog(msg)

disambiguateQuery = (mode, text, rel, participantIds, pos) ->
  participants = participantIds.join(" ")
  params = "text=" + text
  params += "&mode=" + mode
  params += "&rel=" + encodeURIComponent(rel)
  params += "&participants=" + encodeURIComponent(participants)
  params += "&pos=" + pos
  $.ajax({
    type: "POST",
    url: "/disambig",
    data: params
    dataType: "text",
    success: disambiguateResultsReceived
  })

disambiguateActionReply = (msg) ->
  aiChatAddLine('<br />GraphBrain: fact updated.')
  window.location.reload()

root = exports ? this
root.disambiguateCreateNode = (mode, text, rel, participants, pos) ->
  params = "&mode=" + mode
  params += "&text=" + encodeURIComponent(text)
  params += "&rel=" + encodeURIComponent(rel)
  params += "&participants=" + encodeURIComponent(participants)
  params += "&pos=" + pos
  $.ajax({
    type: "POST",
    url: "/disambig_create",
    data: params
    dataType: "text",
    success: disambiguateActionReply
  })
  hideDisambiguateDialog()

root.disambiguateChangeNode = (mode, rel, participants, pos, changeTo) ->
  params = "&mode=" + mode
  params += "&rel=" + encodeURIComponent(rel)
  params += "&participants=" + encodeURIComponent(participants)
  params += "&pos=" + pos
  params += "&changeto=" + encodeURIComponent(changeTo)
  $.ajax({
    type: "POST",
    url: "/disambig_change",
    data: params
    dataType: "text",
    success: disambiguateActionReply
  })
  hideDisambiguateDialog()