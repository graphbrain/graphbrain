package com.graphbrain.db

case class EdgeType(override val id: String,
                    label: String="",
                    override val degree: Int = 0,
                    override val ts: Long = -1)
  extends Vertex(id, degree, ts) {

  def this(id: String, map: Map[String, String]) =
    this(id,
      map("label"),
      map("degree").toInt,
      map("ts").toLong)

  override def extraMap = Map("label" -> label)
}