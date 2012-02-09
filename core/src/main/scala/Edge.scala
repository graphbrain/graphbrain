package com.graphbrain

class Edge(_id: String, val etype: String) extends Vertex(_id) {
  override val vtype = "edge"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("etype" -> etype))

  override def toString: String = super.toString + " " + etype
}

object Edge {
  def apply(_id: String, etype:String) = new Edge(_id, etype) 
}