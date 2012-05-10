initAddDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="addModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Add Connection</h3>
  </div>
  <form class="signupForm">
    <div class="modal-body" id="registerLoginBody">
        <input id="addInput" type="text" value="Aristotle " style="width:90%">
        <span id="nameErrMsg" class="help-inline" />
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

showAddDialog = () ->
  $('#addModal').modal('show')

add = ->

addReply = (msg) ->
  $('#signUpModal').modal('hide')