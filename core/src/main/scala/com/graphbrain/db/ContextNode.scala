package com.graphbrain.db

case class ContextNode(override val id: String,
                       access: String="public",
                       override val summary: String="",
                       override val degree: Int = 0,
                       override val ts: Long = -1)
  extends Textual(id, summary, degree, ts) {

  def this(id: String, map: Map[String, String]) =
    this(id,
      map("access"),
      map("summary"),
      map("degree").toInt,
      map("ts").toLong)

  override def extraMap = Map("access" -> access, "summary" -> summary)

  override def raw: String = {
    "type: " + "context<br />" +
    "id: " + id + "<br />" +
    "access: " + access + "<br />" +
    "summary: " + summary + "<br />"
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