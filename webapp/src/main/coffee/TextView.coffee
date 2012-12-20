# (c) 2012 GraphBrain Ltd. All rigths reserved.

initTextView = ->
    rootText = g.rootNode['text']
    $('#text-view').append('<h2>' + rootText + '</h2><br />')

    for k, v of data['snodes']
        rel = v['label']
        if rel != ''
            nlist = v['nodes']

            relText = rel
            if v['rpos'] == 1
                relText = rel + ' ' + rootText

            $('#text-view').append('<h3>' + relText + ': </h3>')

            first = true
            for n in nlist
              nid = n['id']
              nod = g.snodes[k].nodes[nid]
              text = ''
              if first
                  first = false
              else
                  text = ', '
              text += '<a href="/node/' + nid + '">' + nod['text'] + '</a>'
              $('#text-view').append(text)