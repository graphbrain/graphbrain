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

  override def setId(newId: String): Vertex = copy(id=newId)

  override def setDegree(newDegree: Int): Vertex = copy(degree=newDegree)

  override def setTs(newTs: Long): Vertex = copy(ts=newTs)

  def isNegative = EdgeType.isNegative(id)
}

object EdgeType {
  def isNegative(id: String) = ID.parts(id)(0) == "neg"
}