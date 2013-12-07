createContextModalExists = false


initCreateContextDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="createContextModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Create Context</h3>
  </div>
  <div class="modal-body">
    <form class="form-inline">
      <input type="text" id="contextName" placeholder="Context name">
      <button id="createContextBtn" type="submit" class="btn">Create</button>
    </form>
  </div>
  <div class="modal-footer">
    <a class="btn btn-primary" data-dismiss="modal">Close</a>
  </div>
</div>
    """)
    dialogHtml.appendTo('body')

    $('#createContextBtn').click(createContextSubmit)


showCreateContextDialog = (msg) ->
  if not createContextModalExists
    createContextModalExists = true
    initCreateContextDialog()
  $('#createContextModal').modal('show')


hideCreateContextDialog = (msg) ->
  $('#createContextModal').modal('hide')


createContextSubmit = (msg) ->
  $.ajax({
    type: "POST",
    url: "/createcontext",
    data: "name=" + $("#contextName").val(),
    dataType: "json",
    success: contextCreateReply
  })
  false


contextCreateReply = (msg) ->
  window.location.reload()


initContextsDropDown = () ->
  for c in data.contexts
    html = '<li><a href="/node/' + c['id'] + '">' + c['name'] + '</a></li>'
    $('#contexts-dropdown').append(html)

    if c.id == data.context
      $("#current-context").html('<i class="icon-globe icon-black"></i> ' + c.name + ' <b class="caret">')