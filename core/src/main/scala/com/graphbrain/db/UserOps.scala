package com.graphbrain.db

trait UserOps extends Graph {

  private def addLinkToGlobal(globalId: String, userId: String) = {
    logger.debug(s"addLinkToGlobal globalNodeId: $globalId; userNodeId: $userId")
    back.addLinkToGlobal(globalId, userId)
  }

  private def removeLinkToGlobal(globalId: String, userId: String) = {
    logger.debug(s"removeLinkToGlobal globalNodeId: $globalId; userNodeId: $userId")
    back.removeLinkToGlobal(globalId, userId)
  }

  def put(vertex: Vertex, userid: String): Vertex = {
    logger.debug(s"put $vertex; userId: $userid")
    if (vertex.shouldUpdate(this)) {
      logger.debug(s"should update: $vertex")
      put(vertex)
    }
    if (!ID.isInUserSpace(vertex.id)) {
      val uVertex = vertex.toUser(userid)
      logger.debug(s"uVertex: $uVertex")
      if (!exists(uVertex)) {
        put(uVertex)
        addLinkToGlobal(vertex.id, uVertex.id)
      }
    }

    vertex match {
      case edge: Edge => {
        remove(edge.negate)

        // run consensus algorithm
        edge.toGlobal match {
          case e: Edge => Consensus.evalEdge(e, this)
          case _ =>
        }
      }
      case _ =>
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
    logger.debug(s"getOrInsert: $node; userId: $userId")
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
    logger.debug(s"createAndConnectVertices participants: ${participants.map(_.id).mkString(" ")}; userId: $userId")
    for (v <- participants) {
      val uid = ID.globalToUser(v.id, userId)
      if (!exists(uid)) {
        put(v, userId)
      }
    }
    put(Edge.fromParticipants(participants), userId)
  }

  def connectVertices(participants: Array[String], userId: String) = {
    logger.debug(s"connectVertices participants: ${participants.mkString(" ")}; userId: $userId")
    put(Edge.fromParticipants(participants), userId)
  }

  def edges(centerId: String, userId: String): Set[Edge] = {
    logger.debug(s"edges centerId: $centerId; userId: $userId")

    val gedges = edges(centerId).filter(x => x.isGlobal)
    val uedges = if (userId == null) {
      Set[Edge]()
    }
    else {
      val uCenterId = ID.globalToUser(centerId, userId)
      edges(uCenterId).filter(x => x.isInUserSpace).map(x =>
        x.toGlobal match {
          case e: Edge => e
          case _ => null
        }
      )
    }

    val applyNegatives = gedges.filter(x => !uedges.contains(x.negate))
    val posUEdges = uedges.filter(x => x.isPositive)

    applyNegatives ++ posUEdges
  }

  def globalAlts(globalId: String) = {
    //ldebug("globalAlts: " + globalId)
    back.alts(globalId)
  }
}