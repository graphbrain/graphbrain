package com.graphbrain.hgdb

class Edge(id: String, val etype: String) extends Vertex(id) {
  override val vtype = "edge"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("etype" -> etype))

  override def toString: String = super.toString + " " + etype

  def participantIds = Edge.participantIds(id)
}

object Edge {
  def apply(id: String, etype:String) = new Edge(id, etype)

  def apply(edgeType:String, participants: Array[Node]) = {
  	val tokens = List[String](edgeType) ++ (for (node <- participants) yield node.id)
  	val eid = tokens.reduceLeft(_ + " " + _)
  	new Edge(eid, edgeType)
  }

  def participantIds(id: String) = {
  	val tokens = id.split(' ')
  	for (i <- 1 until tokens.length) yield tokens(i)
  } 
}