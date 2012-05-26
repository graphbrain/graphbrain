initAddDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="addModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Add Connection</h3>
  </div>
  <form id="addForm" action="/add" method="post">
    <div class="modal-body" id="addBody">
      <div id="leftRight">
        <div style="margin-bottom:20px">
          <div class="snodeDialog"><div class="node">""" + nodes[rootNodeId]['text'] + """</div></div>
          <div class="linkDialog"><div class="linkText" id="relationRight">...</div><div class="linkArrow" /></div>
          <div class="snodeDialog"><div class="node" id="newNodeRight">?</div></div>
          <button id="rightLeftBtn" class="btn btn-inverse" style="margin:15px" href="#"><i class="icon-arrow-left icon-white"></i></button>
        </div>
        <div style="clear:both">
          <label>Relation</label>
          <input id="addRelation" name="relation" type="text" placeholder="..." style="width:90%">
          <label>Enter text or URL</label>
          <input id="addInput" name="textUrl" type="text" placeholder="?" style="width:90%">
        </div>
      </div>
      <div id="rightLeft">
        <div style="margin-bottom:20px">
          <div class="snodeDialog"><div class="node">""" + nodes[rootNodeId]['text'] + """</div></div>
          <div class="linkDialog"><div class="linkArrowLeft" /><div class="linkText" id="relationLeft">...</div></div>
          <div class="snodeDialog"><div class="node" id="newNodeLeft">?</div></div>
          <button id="leftRightBtn" class="btn btn-inverse" style="margin:15px" href="#"><i class="icon-arrow-right icon-white"></i></button>
        </div>
        <div style="clear:both">
          <label>Enter text or URL</label>
          <input id="addInput2" name="textUrl" type="text" placeholder="?" style="width:90%">
          <label>Relation</label>
          <input id="addRelation2" name="relation" type="text" placeholder="..." style="width:90%">
        </div>
      </div>
      <label>Brain</label>
      <div class="controls">
        <select id="addDialogSelectBrain" name="brainId"></select>
      </div>
      <input name="curBrainId" type="hidden" value='""" + curBrainId + """' />
      <input name="rootId" type="hidden" value='""" + rootNodeId + """' />
      <input id="direction" name="direction" type="hidden" value="right" />
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
    setLeftRight()
    $('#addInput').keyup(updateAddInput1)
    $('#addRelation').keyup(updateAddRelation1)
    $('#addInput2').keyup(updateAddInput2)
    $('#addRelation2').keyup(updateAddRelation2)
    $('#addButton').click(add)
    $('#leftRightBtn').click(setLeftRight)
    $('#rightLeftBtn').click(setRightLeft)

setLeftRight = () ->
  $('#leftRight').css('display', 'inline')
  $('#rightLeft').css('display', 'none')
  $('#direction').val('right')
  false

setRightLeft = () ->
  $('#leftRight').css('display', 'none')
  $('#rightLeft').css('display', 'inline')
  $('#direction').val('left')
  false

showAddDialog = () ->
  $('#addModal').modal('show')

add = ->
  $('#addForm').submit()

addReply = (msg) ->
  $('#signUpModal').modal('hide')

updateAddInput = () ->
    $("#newNodeRight").html($("#addInput").val())
    $("#newNodeLeft").html($("#addInput").val())

updateAddRelation = () ->
    $("#relationLeft").html($("#addRelation").val())
    $("#relationRight").html($("#addRelation").val())

updateAddInput1 = (msg) ->
    $("#addInput2").val($("#addInput").val())
    updateAddInput()

updateAddRelation1 = (msg) ->
    $("#addRelation2").val($("#addRelation").val())
    updateAddRelation()

updateAddInput2 = (msg) ->
    $("#addInput").val($("#addInput2").val())
    updateAddInput()

updateAddRelation2 = (msg) ->
    $("#addRelation").val($("#addRelation2").val())
    updateAddRelation()