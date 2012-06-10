package com.graphbrain.hgdb


trait UIOps extends NodeManagement {

  def removeVertexAndEdgesUI(vertex: Vertex) = {
    vertex match {
      case b: Brain => {
        val userId = brainOwner(b.id)
        val user = getUserNode(userId)

        // remove user->brain edge
        delrel("brain", Array(userId, b.id))
        // remove brain from user vertex
        put(user.setBrains(user.brains - b.id))
      }
      case u: UserNode => // don't delete usernodes
      case _ => removeVertexAndEdges(vertex)
    }
  }
}