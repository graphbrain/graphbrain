initRelations = () ->
  html = ""

  rels = data['allrelations']

  for r in rels
    html += g.label(r['label'], r['pos']) + '<br />'

  $('#rel-list').html(html)