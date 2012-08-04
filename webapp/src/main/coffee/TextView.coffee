# (c) 2012 GraphBrain Ltd. All rigths reserved.

initTextView = ->
    rootText = nodes[rootNodeId]['text']
    $('#text-view').append('<h2>' + rootText + '</h2><br />')

    for sn in snodes
        rel = sn['rel']
        if rel != ''
            nlist = sn['nodes']

            relText = rel
            if sn['rpos'] == 1
                relText = rel + ' ' + rootText

            $('#text-view').append('<h3>' + relText + ': </h3>')

            first = true
            for nid in nlist
              nod = nodes[nid]
              text = ''
              if first
                  first = false
              else
                  text = ', '
              text += '<a href="/node/' + nid + '">' + nod['text'] + '</a>'
              $('#text-view').append(text)