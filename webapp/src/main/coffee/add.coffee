initAddDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="addModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Add Connection</h3>
  </div>
  <form id="addForm" action="/add" method="post">
    <div class="modal-body" id="addBody">
        <div class="node" style="display:inline; float:left">""" + nodes[rootNodeId]['text'] + """</div>
        <div class="linkLabel" style="position:relative; float:left"><div class="linkText" id="relation">...</div><div class="linkArrow" /></div>
        <div class="node" id="newNode" style="display:inline; float:left">?</div>
        <br /><br />
        <label>Enter text or URL</label>
        <input id="addInput" name="textUrl" type="text" placeholder="?" style="width:90%">
        <label>Relation</label>
        <input id="addRelation" name="relation" type="text" placeholder="..." style="width:90%">
        <label>Brain</label>
        <div class="controls">
          <select id="addDialogSelectBrain" name="brainId"></select>
        </div>
        <input name="curBrainId" type="hidden" value='""" + curBrainId + """' />
        <input name="rootId" type="hidden" value='""" + rootNodeId + """' />
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
  $('#addForm').submit()

addReply = (msg) ->
  $('#signUpModal').modal('hide')

updateAddInput = (msg) ->
    $("#newNode").html($("#addInput").val())

updateAddRelation = (msg) ->
    $("#relation").html($("#addRelation").val())