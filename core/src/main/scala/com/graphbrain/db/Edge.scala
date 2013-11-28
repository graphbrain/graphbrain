package com.graphbrain.db

case class Edge(elems: Array[Vertex],
                override val degree: Int = 0,
                override val ts: Long = -1)
  extends Vertex("(" + elems.map(_.toString).reduceLeft(_ + " " + _) + ")", degree, ts) {

  val ids = elems.map(_.toString)
  val edgeType = ids.head
  val participantIds = ids.tail

  def this(id: String, map: Map[String, String]) =
    this(Edge.elemsFromId(id),
      map("degree").toInt,
      map("ts").toLong)

  override def extraMap = Map()

  override def setId(newId: String): Vertex = Edge.fromId(newId)

  override def setDegree(newDegree: Int): Vertex = copy(degree=newDegree)

  override def setTs(newTs: Long): Vertex = copy(ts=newTs)

  def negate = Edge.fromParticipants("neg/" + edgeType, participantIds)

  def isNegative = EdgeType.isNegative(edgeType)

  def isPositive = !isNegative

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

  override def toUser(userId: String): Vertex = {
    val pids = for (pid <- ids) yield ID.globalToUser(pid, userId)
    Edge.fromParticipants(pids)
  }
  
  override def toGlobal: Vertex = {
    val pids = for (pid <- ids) yield ID.userToGlobal(pid)
    Edge.fromParticipants(pids)
  }

  def matches(pattern: Edge): Boolean = {
    for (i <- 0 until ids.length)
      if ((pattern.ids(i) != "*") && (pattern.ids(i) != ids(i)))
        return false

    true
  }

  def humanReadable2 = (ID.humanReadable(participantIds(0)) +
                        " [" +  ID.humanReadable(edgeType) + "] " +
                        ID.humanReadable(participantIds(1))).replace(",", "")
}

object Edge {
  def fromId(id: String) = EdgeParser(id) match {
    case e: Edge => e
    case _ => null
  }

  def elemsFromId(id: String) =
    fromId(id).elems

  def fromParticipants(participants: Array[String]) =
    new Edge(participants.map(Vertex.fromId))

  def fromParticipants(edgeType: String, participantIds: Array[String]) =
    new Edge(Array(Vertex.fromId(edgeType)) ++ participantIds.map(Vertex.fromId))
}