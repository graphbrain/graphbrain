package com.graphbrain.db

case class Edge(override val id: String,
                override val degree: Int = 0,
                override val ts: Long = -1)
  extends Vertex(id, degree, ts) {

  val ids  = ID.parts(id)
  val edgeType = ids.head
  val participantIds = ids.tail

  def this(id: String, map: Map[String, String]) =
    this(id,
      map("degree").toInt,
      map("ts").toLong)

  override def extraMap = Map()

  def negate = Edge.fromParticipants("neg/" + edgeType, participantIds)

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

  def isInContextSpace: Boolean = {
    for (p <- participantIds)
      if (!ID.isInContextSpace(p))
        return false

    true
  }

  override def toUser(userId: String) = {
    val pids = for (pid <- participantIds) yield ID.globalToUser(pid, userId)
    Edge.fromParticipants(edgeType, pids)
  }
  
  override def toGlobal = {
    val pids = for (pid <- participantIds) yield ID.userToGlobal(pid)
    Edge.fromParticipants(edgeType, pids)
  }

  override def toString = edgeType + " " + participantIds.reduceLeft(_ + " " + _)

  def humanReadable2 = (ID.humanReadable(participantIds(0)) +
                        " [" +  ID.humanReadable(edgeType) + "] " +
                        ID.humanReadable(participantIds(1))).replace(",", "")
}

object Edge {
  def fromParticipants(participants: Array[String]) =
    new Edge(idFromParticipants(participants))

  def fromParticipants(participants: Array[Vertex]) =
    new Edge(idFromParticipants(participants))

  def fromParticipants(edgeType: String, participantIds: Array[String]) =
    new Edge(idFromParticipants(edgeType, participantIds))

  def idFromParticipants(participants: Array[String]) = participants.mkString(" ")

  def idFromParticipants(participants: Array[Vertex]) =
    idFromParticipants(participants.map(v => v.id))

  def idFromParticipants(edgeType: String, participantIds: Array[String]) =
    idFromParticipants(Array(edgeType) ++ participantIds)
}