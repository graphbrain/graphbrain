package com.graphbrain.hgdb


trait NodeManagement extends VertexStoreInterface {
  def createAndConnectVertices(edgeType: String, participants: Array[Vertex]) = {
    for (v <- participants) {
      if (!exists(v.id)) {
        put(v)
      }
    }

    val ids = for (v <- participants) yield v.id
    addrel(edgeType.replace(" ", "_"), ids)
  }

  def removeVertexAndEdges(vertex: Vertex) = {
    var curEdgeList = vertex
    
    // iterate through extra edges
    var extra = 1
    var done = false
    while (!done) {
      // remove each edge
      for (edge <- curEdgeList.edges) {
        val edgeType = Edge.edgeType(edge)
        val participants = Edge.participantIds(edge).toArray
        delrel(edgeType, participants)
      }

      val extraId = VertexStore.extraId(vertex.id, extra)
      if (exists(extraId)) {
        curEdgeList = get(extraId)
        extra += 1
      }
      else {
        done = true
      }
    }

    // remove vertex
    remove(vertex)
  }

  def nodeOwner(nodeId: String): String = {    
    val tokens = nodeId.split("/")
    if (tokens(0) == "user") {
      "user/" + tokens(1)  
    }
    else {
      ""
    }
  }
}