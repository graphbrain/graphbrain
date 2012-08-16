removeMode = false

removeInfoMessage = ->
    setInfoAlert('<strong>Click on the item</strong> you want to remove.')

removeButtonPressed = (msg) ->
    if removeMode
        hideAlert()
        removeMode = false
    else
        removeInfoMessage()
        removeMode = true
    true

initRemoveDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="removeModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Remove</h3>
  </div>
  <form id="removeForm" action='/node/""" + rootNodeId + """' method="post">
    <input type="hidden" name="op" value="remove">
    <input id="removeNodeField" type="hidden" name="node">
    <input id="removeOrigField" type="hidden" name="orig">
    <input id="removeLinkField" type="hidden" name="link">
    <input id="removeTargField" type="hidden" name="targ">
    <div class="modal-body" id="addBrainBody">
        <div id="linkDesc"></div>
        <label class="radio">
            <input id="linkRadio" type="radio" name="linkOrNode" value="link">
            Just remove this connection
        </label>
        <br />
        <div id="itemDesc">Item</div>
        <label class="radio">
            <input id="nodeRadio" type="radio" name="linkOrNode" value="node">
            Remove this item and all associated connections
        </label>
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

showRemoveDialog = (node, orig, link, targ, etype) ->
  if node == rootNodeId
    setErrorAlert('You cannot remove the item in the center.')
  else
    $('#linkRadio').attr('checked', true)
    $('#nodeRadio').attr('checked', false)
    $('#linkRadio').attr('disabled', false)
    $('#nodeRadio').attr('disabled', false)

    # user nodes cannot be deleted
    if nodes[node].type == 'user'
        $('#nodeRadio').attr('disabled', true)
    # links between user and brain cannot be deleted
    else if (link == 'brain') && (nodes[orig].type == 'user') && (nodes[targ].type == 'brain')
        $('#linkRadio').attr('disabled', true)
        $('#linkRadio').attr('checked', false)
        $('#nodeRadio').attr('checked', true)
    removeInfoMessage()
    $('#removeNodeField').val(node)
    $('#removeOrigField').val(orig)
    $('#removeLinkField').val(etype)
    $('#removeTargField').val(targ)
    $('#linkDesc').html(nodes[orig].text + ' <strong>' + link + '</strong> ' + nodes[targ].text)
    $('#itemDesc').html(nodes[node].text)
    $('#removeModal').modal('show')

removeAction = ->
  $('#removeForm').submit()
