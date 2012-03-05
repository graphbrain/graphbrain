package com.graphbrain.hgdb

class Edge(id: String, val etype: String, edges: Set[String]) extends Vertex(id, edges) {
  override def toMap: Map[String, Any] = Map(("vtype" -> "edge")) ++ toMapBase
  override def toMapBase: Map[String, Any] = super.toMapBase ++ Map(("etype" -> etype))

  override def addEdge(edgeId: String): Vertex = new Edge(id, etype, edges + edgeId)
  override def delEdge(edgeId: String): Vertex = new Edge(id, etype, edges - edgeId)

  override def toString: String = super.toString + " " + etype

  def participantIds = Edge.participantIds(id)
}

object Edge {
  def apply(id: String, etype: String, edgesStr: String) = new Edge(id, etype, Vertex.str2iter(edgesStr).toSet)

  def apply(etype:String, participants: Array[String]) = {
  	val tokens = List[String](etype) ++ participants
  	val eid = tokens.reduceLeft(_ + " " + _)
  	new Edge(eid, etype, Set[String]())
  }

  def participantIds(id: String) = {
  	val tokens = id.split(' ')
  	for (i <- 1 until tokens.length) yield tokens(i)
  } 
}