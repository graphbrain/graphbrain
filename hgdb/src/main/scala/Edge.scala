package com.graphbrain.hgdb


case class Edge(edgeType: String, extParticipantIds: List[String], originalEdge: Edge = null) {
  val participantIds = extParticipantIds.map(x => Vertex.cleanId(x))

  def negate = Edge("neg/" + edgeType, extParticipantIds)

  def isPositive = ID.parts(edgeType)(0) != "neg"

  def isGlobal: Boolean = {
    for (p <- participantIds)
      if (!ID.isUserNode(p) && ID.isInUserSpace(p))
        return false

    true
  }

  def isInUserSpace: Boolean = {
    for (p <- participantIds)
      if (ID.isInUserSpace(p))
        return true

    false
  }

  def toUser(userId: String) = {
    val pids = for (pid <- extParticipantIds) yield ID.globalToUser(pid, userId)
    Edge(edgeType, pids)
  }
  
  def toGlobal = {
    val pids = for (pid <- extParticipantIds) yield ID.userToGlobal(pid)
    Edge(edgeType, pids)
  }

  def getOriginalEdge = if (originalEdge == null) this else originalEdge

  override def toString = edgeType + " " + participantIds.reduceLeft(_ + " " + _)
}

object Edge {
  def fromString(edgeString: String) = {
    val tokens = edgeString.split(" ").toList
    Edge(tokens.head, tokens.tail)
  }

  def fromEdgeEntry(nodeId: String, edgeType: String, position: Int, node1: String, node2: String, nodeN: String) = {
    val preParticipantIds = if (nodeN != null) {
        List[String](node1, node2) ::: nodeN.split(" ").toList
      }
      else if (node2 != null) {
        List[String](node1, node2)
      }
      else {
        List[String](node1)
      }

    val participantIds = if (position == 0) {
        nodeId :: preParticipantIds
      }
      else if (position >= preParticipantIds.length) {
        preParticipantIds :+ nodeId
      }
      else {
        val parts = preParticipantIds.splitAt(position)
        parts._1 ::: nodeId :: parts._2
      }

    Edge(edgeType, participantIds)
  }
}