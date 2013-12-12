class State
    constructor: () ->
      @values = {}
      if localStorage.getItem('newedges') != null
        @values['newedges'] = JSON.parse(localStorage.getItem('newedges'))


    setNewEdges: (newedges) ->
        localStorage.setItem('newedges', JSON.stringify(newedges))
        @values['newedges'] = newedges


    getNewEdges: ->
        @values['newedges']


    clean: ->
        localStorage.removeItem('newedges')