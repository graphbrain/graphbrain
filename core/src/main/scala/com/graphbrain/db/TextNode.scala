package com.graphbrain.db

case class TextNode(override val id: String,
                    override val summary: String="",
                    override val degree: Int = 0,
                    override val ts: Long = -1)
  extends Textual(id, summary, degree, ts) {

  def this(id: String, map: Map[String, String]) =
    this(id,
      map("summary"),
      map("degree").toInt,
      map("ts").toLong)

  override def extraMap = Map("summary" -> summary)

  override def setId(newId: String): Vertex = copy(id=newId)

  override def setDegree(newDegree: Int): Vertex = copy(degree=newDegree)

  override def setTs(newTs: Long): Vertex = copy(ts=newTs)

  /*
  override def updateSummary: Textual = TextNode(namespace, text, generateSummary)


  override def updateFromEdges(): Vertex = {
    val newSummary = generateSummary
    val newVertex = this.copy(summary=newSummary)

    if (this != newVertex) {
      put()
    }

    this
  }*/

  override def raw: String = {
    "type: " + "text<br />" +
    "id: " + id + "<br />" +
    "summary: " + summary + "<br />"
  }
}


object TextNode {
  def fromNsAndText(namespace: String,
    text: String,
    summary: String="",
    degree: Int = 0,
    ts: Long = -1) = {

    TextNode(namespace + "/" + ID.sanitize(text).toLowerCase,
      summary,
      degree,
      ts)
  }
}