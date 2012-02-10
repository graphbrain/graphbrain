package com.graphbrain

class EdgeType(_id: String, val label: String) extends Vertex(_id) {
  override val vtype = "edgeType"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("label" -> label))
}

object EdgeType {
  def apply(_id: String, label: String) = new EdgeType(_id, label) 
}