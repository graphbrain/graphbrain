package com.graphbrain

class Node(_id: String, val edges: Set[String]) extends Vertex(_id) {
  override val vtype = "node"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("edges" -> edges))

  def addEdge(edge: Edge): Node = Node(_id, edges + edge._id)

  def delEdge(edge: Edge): Node = Node(_id, edges - edge._id)

  /*
  def neighbors(nodes: Map[String, Node] = Map[String, Node](), depth: Int = 0, maxDepth: Int = 2):
  	if (depth < maxDepth) {
  		val coedgeIds = for (edge <- edges) yield 
  	}
  	else {
  		nodes
  	}
  }
  */

}

object Node {
  def apply(_id: String, edges: Set[String] = Set[String]()) = new Node(_id, edges) 
}