package com.graphbrain.hgdb

class Edge(id: String, val etype: String) extends Vertex(id) {
  override val vtype = "edge"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("etype" -> etype))

  override def toString: String = super.toString + " " + etype

  def participantIds = Edge.participantIds(id)
}

object Edge {
  def apply(id: String, etype:String) = new Edge(id, etype)

  def apply(etype:String, participants: Array[String]) = {
  	val tokens = List[String](etype) ++ participants
  	val eid = tokens.reduceLeft(_ + " " + _)
  	new Edge(eid, etype)
  }

  def participantIds(id: String) = {
  	val tokens = id.split(' ')
  	for (i <- 1 until tokens.length) yield tokens(i)
  } 
}