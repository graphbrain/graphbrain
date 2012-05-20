initAddBrainDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="addBrainModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Add Brain</h3>
  </div>
  <form id="addBrainForm" action="/addbrain" method="post">
    <div class="modal-body" id="addBrainBody">
        <label>Brain name</label>
        <input id="brainNameInput" type="text" name="name" style="width:90%">
    </div>
    <div class="modal-footer">
      <a class="btn" data-dismiss="modal">Close</a>
      <a id="addBrainButton" class="btn btn-primary">Add</a>
    </div>
  </form>
</div>
    """)
    dialogHtml.appendTo('body')
    $('#addBrainButton').click(addBrain)

showAddBrainDialog = () ->
  $('#addBrainModal').modal('show')

addBrain = ->
  $('#addBrainForm').submit()