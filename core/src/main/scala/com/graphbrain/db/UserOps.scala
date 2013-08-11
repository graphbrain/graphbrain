package com.graphbrain.db

trait UserOps extends Graph {

  private def addLinkToGlobal(globalId: String, userId: String) = {
    ldebug("addLinkToGlobal globalNodeId: " + globalId + "; userNodeId: " + userId)
    back.addLinkToGlobal(globalId, userId)
  }

  private def removeLinkToGlobal(globalId: String, userId: String) = {
    ldebug("removeLinkToGlobal globalNodeId: " + globalId + "; userNodeId: " + userId)
    back.removeLinkToGlobal(globalId, userId)
  }

  def put(vertex: Vertex, userid: String): Vertex = {
    ldebug("put2 " + vertex + "; userId: " + userid)
    if (vertex.shouldUpdate(this)) {
      ldebug("should update " + vertex)
      put(vertex)
    }
    if (!ID.isInUserSpace(vertex.id)) {
      val userSpaceId = ID.globalToUser(vertex.id, userid)
      if (!exists(userSpaceId)) {
        put(get(vertex.id).toUser(userid))
        addLinkToGlobal(vertex.id, userSpaceId)
      }
    }

    vertex match {
      case edge: Edge => {
        remove(edge.negate)

        // run consensus algorithm
        Consensus.evalEdge(edge.toGlobal, this)
      }
    }

    vertex
  }

  def remove(vertex: Vertex, userId: String): Unit = {

    val u = vertex.toUser(userId)

    // delete from user space
    remove(u)
    removeLinkToGlobal(vertex.id, u.id)

    vertex match {
      case u: Edge => {
        // create negation of edge in user space
        put(u.negate)
        // run consensus algorithm
        Consensus.evalEdge(u, this)
      }
    }
  }

  def getOrInsert(node: Vertex, userId: String): Vertex = {
    ldebug("getOrInsert " + node + "; userId: " + userId)
    val g = get(node.id)
    val u = get(ID.globalToUser(node.id, userId))
    if ((g == null) || (u == null)) {
        put(node, userId)
        get(node.id)
    }
    else {
      g
    }
  }

  def createAndConnectVertices(participants: Array[Vertex], userId: String) = {
    //ldebug("createAndConnectVertices edgeType: " + edgeType + "; participants: " + participants + "; userid: " + userid + "; consensus: " + consensus)
    for (v <- participants) {
      val uid = ID.globalToUser(v.id, userId)
      if (!exists(uid)) {
        put(v, userId)
      }
    }
    put(Edge.fromParticipants(participants), userId)
  }

  def edges(centerId: String, userId: String): Set[Edge] = {
    //ldebug("neighborEdges2 nodeId: " + nodeId + "; userid: " + userid + "; edgeType: " + edgeType + "; pos: " + relPos)

    // global space
    val uCenterId = ID.globalToUser(centerId, userId)

    val gedges = edges(centerId).filter(x => x.isGlobal)
    val uedges = edges(uCenterId).filter(x => x.isInUserSpace).map(x => x.toGlobal)

    val applyNegatives = gedges.filter(x => !uedges.contains(x.negate))
    val posUEdges = uedges.filter(x => x.isPositive)

    applyNegatives ++ posUEdges
  }

  def globalAlts(globalId: String) = {
    //ldebug("globalAlts: " + globalId)
    back.alts(globalId)
  }
}