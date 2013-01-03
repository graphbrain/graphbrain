removeClicked = (msg) ->
    showRemoveDialog(msg.data.node, msg.data.link, msg.data.edge)

initRemoveDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="removeModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Confirm Removal</h3>
  </div>
  <form id="removeForm" action='/node/""" + rootNodeId + """' method="post">
    <input type="hidden" name="op" value="remove">
    <input id="removeEdgeField" type="hidden" name="edge">
    <div class="modal-body" id="addBrainBody">
        <div id="linkDesc"></div>
    </div>
    <div class="modal-footer">
      <a class="btn" data-dismiss="modal">Close</a>
      <a id="removeDlgButton" class="btn btn-primary">Remove</a>
    </div>
  </form>
</div>
    """)
    dialogHtml.appendTo('body')
    $('#removeDlgButton').click(removeAction)

showRemoveDialog = (node, link, edge) ->
    $('#removeEdgeField').val(edge)
    $('#linkDesc').html(node.text + ' <strong>(' + link + '</strong>)')
    $('#removeModal').modal('show')

removeAction = ->
  $('#removeForm').submit()
