initAddFriendDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="addFriendModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Add Connection</h3>
  </div>
  <form class="addFriendForm">
    <div class="modal-body" id="addBody">
        <label>Name</label>
        <input id="addInput" type="text" placeholder="" style="width:90%">
        <label>Email</label>
        <input id="addRelation" type="text" placeholder="" style="width:90%">
    </div>
    <div class="modal-footer">
      </form>
      <a class="btn" data-dismiss="modal">Close</a>
      <a id="addFriendButton" class="btn btn-primary">Add</a>
    </div>
  </form>
</div>
    """)
    dialogHtml.appendTo('body')
    $('#addFriend').click(add)

showAddFriendDialog = () ->
  $('#addFriendModal').modal('show')

addFriend = ->