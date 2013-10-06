package com.graphbrain.db

case class ContextNode(override val id: String,
                       access: String="public",
                       override val degree: Int = 0,
                       override val ts: Long = -1)
  extends Vertex(id, degree, ts) {

  def this(id: String, map: Map[String, String]) =
    this(id,
      map("access"),
      map("degree").toInt,
      map("ts").toLong)

  override def extraMap = Map("access" -> access)

  override def setId(newId: String): Vertex = copy(id=newId)

  override def setDegree(newDegree: Int): Vertex = copy(degree=newDegree)

  override def setTs(newTs: Long): Vertex = copy(ts=newTs)

  override def raw: String = {
    "type: " + "context<br />" +
    "id: " + id + "<br />" +
    "access: " + access + "<br />"
  }
}

object ContextNode {
  def fromUserAndName(userId: String,
                      name: String,
                      access: String="public") = {
    val id = userId + "/context/" + ID.sanitize(name).toLowerCase
    ContextNode(id, access)
  }
}