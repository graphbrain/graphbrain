package com.graphbrain.hgdb


object Consensus {
  def evalEdge(edge: Edge, store: UserOps) = {
    var score = 0

    val negEdge = edge.negate
    val pids = edge.participantIds

    // go through all the user space alt version of the first version in this edge
    // TODO: select edge participant with the lowest degree?
    val altVertices = store.globalAlts(pids(0))
    for (altV <- altVertices) {
      val userId = ID.ownerId(altV)

      // generate edges with user space participants owned by this user id
      val userEdge = edge.toUser(userId)
      val negUserEdge = negEdge.toUser(userId)

      // update score based on postive and negative user space edges
      if (store.relExists(userEdge)) {
        score += 1
      }
      if (store.relExists(negUserEdge)) {
        score -= 1
      }
    }

    // add or delete edge based on consensus score
    if (score > 0) {
      store.addrel(edge)
    }
    else {
      store.delrel(edge)
    }
  }
}
