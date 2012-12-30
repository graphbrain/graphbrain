initRelations = () ->
  html = ""

  rels = data['allrelations']

  count = 0
  for r in rels
    label = g.label(r['label'], r['pos']) + '<br />'
    html += '<a href="#" id="rel' + count + '">' + label + '</a>'
    count += 1

  $('#rel-list').html(html)

  # events relation links
  count = 0
  for r in rels
    eventData = {rel: r['rel'], pos: r['pos']}
    $('#rel' + count).bind 'click', eventData, relationSubmit
    count += 1

relationSubmit = (msg) ->
  eventData = msg.data
  $.ajax({
    type: "POST",
    url: "/rel",
    data: "rel=" + eventData.rel + "&pos=" + eventData.pos +  "&rootId=" + rootNodeId,
    dataType: "json",
    success: relationReply
  })
  false


relationReply = (msg) ->
  #reply = $.parseJSON(msg)
  alert('server reply: ' + msg['rel'])