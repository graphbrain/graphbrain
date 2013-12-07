initRelations = () ->
  html = ""

  rels = data['allrelations']

  count = 0
  for r in rels
    label = g.label(r['label'], r['pos']) + '<br />'
    if g.snodes[r['snode']] == undefined
      html += '<a class="visible_rel_link" href="#" id="rel' + count + '">' + label + '</a>'
    else
      html += '<a class="hidden_rel_link" href="#" id="rel' + count + '">' + label + '</a>'
    count += 1

  $('#rel-list').html(html)

  # events relation links
  count = 0
  for r in rels
    eventData = {rel: r['rel'], pos: r['pos'], snode: r['snode']}
    $('#rel' + count).bind 'click', eventData, relationSubmit
    count += 1


relationSubmit = (msg) ->
  eventData = msg.data
  if g.snodes[eventData.snode] == undefined
    $.ajax({
      type: "POST",
      url: "/rel",
      data: "rel=" + eventData.rel + "&pos=" + eventData.pos +  "&rootId=" + rootNodeId,
      dataType: "json",
      success: relationReply
    })
  else
    addAnim(new AnimLookAt(g.snodes[eventData.snode]))
  false


relationReply = (msg) ->
  g.addSNodesFromJSON(msg)
  initRelations()
  
  sid = ''
  for k, v of msg['snodes']
    sid = k

  if sid != ''
    snode = g.snodes[sid]
    addAnim(new AnimLookAt(snode))