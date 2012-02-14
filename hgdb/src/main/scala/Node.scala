package com.graphbrain.hgdb

class Node(id: String, val edges: Set[String]) extends Vertex(id) {
  override val vtype = "node"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("edges" -> Node.set2str(edges)))

  def addEdge(edge: Edge): Node = Node(id, edges + edge.id)

  def delEdge(edge: Edge): Node = Node(id, edges - edge.id)

  def neighbors(nodes: Map[String, Node] = Map[String, Node](), depth: Int = 0, maxDepth: Int = 2) = {
  	if (depth < maxDepth) {
  		val coedgeIds = (for (edgeId <- edges) yield Edge.participantIds(edgeId)).reduceLeft(_ ++ _)
  		nodes
  	}
  	else {
  		nodes
  	}
  }
}

object Node {
  def apply(id: String, edges: Set[String] = Set[String]()) = new Node(id, edges)

  def apply(id: String, edgesStr: String) = new Node(id, str2set(edgesStr))

  def set2str(set: Set[String]) = if (set.size == 0) "" else set.reduceLeft(_ + " " + _)

  def str2set(str: String) = str.split(' ').toSet
}