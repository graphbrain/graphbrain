initRelations = () ->
  html = ""

  rels = data['allrelations']

  count = 0
  for r in rels
    label = g.label(r['label'], r['pos']) + '<br />'
    if g.snodes[r['snode']] == undefined
      html += '<a href="#" id="rel' + count + '">' + label + '</a>'
    else
      html += label
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
  g.addSNodesFromJSON(msg)
  initRelations()
  
  sid = ''
  for k, v of msg['snodes']
    sid = k

  if sid != ''
    snode = g.snodes[sid]
    lookToSNode(snode)