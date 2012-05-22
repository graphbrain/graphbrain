brainMap = {}

setCurBrain = (name, access) ->
  html = '<i class="icon-eye-open icon-white"></i> ' + name + ' <b class="caret">'
  $('#curBrain').html(html)

initBrains = () ->
  html = '<li><a href="/node/' + userId +  '">Home</a></li>'
  selectHtml = '<option>Home</option>'
  if curBrainId == userId
      setCurBrain('Home', 'home')
  brainMap[userId] = {'name': 'Home', 'access': 'home'}
  for brain in brains
    brainMap[brain['id']] = {'name': brain['name'], 'access': brain['access']}
    html += '<li><a href="/node/' + brain['id'] +  '">' + brain['name'] + '</a></li>'
    if curBrainId == brain['id']
      setCurBrain(brain['name'], brain['access'])
      selectHtml += '<option selected>' + brain['name'] + '</option>'
    else
      selectHtml += '<option>' + brain['name'] + '</option>'
      
  $('#brainDropdown').html(html)
  $('#addDialogSelectBrain').html(selectHtml)

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