package com.graphbrain.hgdb


object Consensus {
  def evalEdge(edgeId: String, store: VertexStore) = {
    var score = 0

    val negEdgeId = ID.negateEdge(edgeId)
    val pids = Edge.participantIds(edgeId)

    // go through all the user space alt version of the first version in this edge
    val altEdges = store.getEdges(pids(0), 0, "sys/alt")
    for (altE <- altEdges) {
      // get the id of the alt version
      val altV = Edge.participantIds(altE)(1)
      // get the user id of the alt version owner
      val userId = ID.ownerId(altV)

      // generate edge ids with user space participants owned by this user id
      val userEdgeId = ID.globalToUserEdge(edgeId, userId)
      val negUserEdgeId = ID.globalToUserEdge(negEdgeId, userId)

      // update score based on postive and negative user space edges
      if (store.exists(userEdgeId)) {
        score += 1
      }
      if (store.exists(negUserEdgeId)) {
        score -= 1
      }
    }

    // add or delete edge based on consensus score
    if (score > 0) {
      store.addrel(edgeId)
    }
    else {
      store.delrel(edgeId)
    }
  }
}
