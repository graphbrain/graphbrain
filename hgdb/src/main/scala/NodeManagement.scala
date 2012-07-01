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
    val nedges = neighborEdges(vertex.id)

    // remove connected edges
    for (edgeId <- nedges) {
      delrel(edgeId)
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