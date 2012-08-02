# (c) 2012 GraphBrain Ltd. All rigths reserved.

initTextView = ->
    $('#text-view').append('<h2>' + nodes[rootNodeId]['text'] + '</h2>')

    for sn in snodes
        rel = sn['rel']
        if rel != ''
            nlist = sn['nodes']

            $('#text-view').append('<h3>' + rel + '</h3>')
        
            for nid in nlist
              nod = nodes[nid]
              text = nod['text']
              $('#text-view').append(text + ' ')