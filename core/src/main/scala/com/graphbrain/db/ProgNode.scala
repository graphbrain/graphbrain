package com.graphbrain.db

case class ProgNode(override val id: String,
                    prog: String="",
                    override val degree: Int = 0,
                    override val ts: Long = -1)
  extends Vertex (id, degree, ts) {

  def this(id: String, map: Map[String, String]) =
    this(id,
      map("prog"),
      map("degree").toInt,
      map("ts").toLong)

  override def extraMap = Map("prog" -> prog)

  override def setId(newId: String): Vertex = copy(id=newId)

  override def setDegree(newDegree: Int): Vertex = copy(degree=newDegree)

  override def setTs(newTs: Long): Vertex = copy(ts=newTs)
}