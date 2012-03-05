package com.graphbrain.hgdb


class Node(id: String, edges: Set[String]) extends Vertex(id, edges) {
  override def toMap: Map[String, Any] = Map(("vtype" -> "node")) ++ toMapBase

  override def addEdge(edgeId: String): Vertex = new Node(id, edges + edgeId)
  override def delEdge(edgeId: String): Vertex = new Node(id, edges - edgeId)
}

object Node {
  def apply(id: String, edges: Set[String] = Set[String]()) = new Node(id, edges)
  def apply(id: String, edgesStr: String) = new Node(id, Vertex.str2iter(edgesStr).toSet)
}