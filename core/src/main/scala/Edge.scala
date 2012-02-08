package com.graphbrain

class Edge(_id: String, links: List[String], val etype: String) extends Vertex(_id, links) {
  override val vtype = "edge"

  override def toMap = super.toMap ++ Map(("etype" -> etype))

  override def toString: String = super.toString + " " + etype
}

object Edge {
  def apply(_id: String, links: List[String], etype:String) = new Edge(_id, links, etype) 
}