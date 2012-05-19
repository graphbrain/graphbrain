initAddDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="addModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Add Connection</h3>
  </div>
  <form class="addForm">
    <div class="modal-body" id="addBody">
        <div class="node" style="display:inline; float:left">Aristotle</div>
        <div class="linkLabel" style="position:relative; float:left"><div class="linkText" id="relation">...</div><div class="linkArrow" /></div>
        <div class="node" id="newNode" style="display:inline; float:left">?</div>
        <br /><br />
        <label>Enter text or URL</label>
        <input id="addInput" type="text" placeholder="?" style="width:90%">
        <label>Relation</label>
        <input id="addRelation" type="text" placeholder="..." style="width:90%">
    </div>
    <div class="modal-footer">
      </form>
      <a class="btn" data-dismiss="modal">Close</a>
      <a id="addButton" class="btn btn-primary">Add</a>
    </div>
  </form>
</div>
    """)
    dialogHtml.appendTo('body')
    $('#addButton').click(add)
    $('#addInput').keyup(updateAddInput)
    $('#addRelation').keyup(updateAddRelation)

showAddDialog = () ->
  $('#addModal').modal('show')

add = ->
  $.ajax({
    type: "POST",
    url: "/nodetxt",
    data: "s=" + $('#addInput').val() + "&r=" + rootNodeId,
    dataType: "text",
    success: @addReply
  })

addReply = (msg) ->
  $('#signUpModal').modal('hide')

updateAddInput = (msg) ->
    $("#newNode").html($("#addInput").val())

updateAddRelation = (msg) ->
    $("#relation").html($("#addRelation").val())