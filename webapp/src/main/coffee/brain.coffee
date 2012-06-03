brainMap = {}


initBrains = () ->
  html = '<i class="icon-eye-open icon-white"></i> ' + curBrainName + ' <b class="caret">'
  $('#curBrain').html(html)

  html = '<li><a href="#" id="addBrainLink">Create Brain</a></li>'
  html += '<li class="divider"></li>'
  html += '<li><a href="/node/' + userId +  '">Home</a></li>'
  selectHtml = '<option>Home</option>'
  brainMap[userId] = {'name': 'Home', 'access': 'home'}
  for brain in brains
    brainMap[brain['id']] = {'name': brain['name'], 'access': brain['access']}
    html += '<li><a href="/node/' + brain['id'] +  '">' + brain['name'] + '</a></li>'
    if curBrainId == brain['id']
      selectHtml += '<option selected>' + brain['name'] + '</option>'
    else
      selectHtml += '<option>' + brain['name'] + '</option>'
      
  $('#brainDropdown').html(html)
  $('#addDialogSelectBrain').html(selectHtml)
  $('#addBrainLink').bind 'click', showAddBrainDialog

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