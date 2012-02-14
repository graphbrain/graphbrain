package com.graphbrain.hgdb

class EdgeType(id: String, val label: String) extends Vertex(id) {
  override val vtype = "edgeType"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("label" -> label))
}

object EdgeType {
  def apply(id: String, label: String) = new EdgeType(id, label) 
}