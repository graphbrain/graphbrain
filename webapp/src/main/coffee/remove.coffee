removeMode = false

removeButtonPressed = (msg) ->
    if removeMode
        hideAlert()
        removeMode = false
    else
        setInfoAlert('<strong>Click on the item</strong> you want to remove.')
        removeMode = true
    true

initRemoveDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="removeModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Remove</h3>
  </div>
  <form id="removeForm" action="/remove" method="post">
    <input id="removeNodeField" type="hidden" name="node">
    <input id="removeOrigField" type="hidden" name="orig">
    <input id="removeLinkField" type="hidden" name="link">
    <input id="removeTargField" type="hidden" name="targ">
    <div class="modal-body" id="addBrainBody">
        <div id="linkDesc"></div>
        <label class="radio">
            <input type="radio" name="linkOrNode" value="link" checked>
            Just remove this connection
        </label>
        <br />
        <div id="itemDesc">Item</div>
        <label class="radio">
            <input type="radio" name="linkOrNode" value="node">
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

showRemoveDialog = (node, orig, link, targ) ->
  $('#removeNodeField').val(node)
  $('#removeOrigField').val(orig)
  $('#removeLinkField').val(link)
  $('#removeTargField').val(targ)
  $('#linkDesc').html(nodes[orig].text + ' <strong>' + link + '</strong> ' + nodes[targ].text)
  $('#itemDesc').html(nodes[node].text)
  $('#removeModal').modal('show')

removeAction = ->
  $('#removeForm').submit()