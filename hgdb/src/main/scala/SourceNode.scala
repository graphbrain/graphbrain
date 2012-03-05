package com.graphbrain.hgdb


class SourceNode(id: String, edges: Set[String]) extends Node(id, edges) {
  override def toMap: Map[String, Any] = Map(("vtype" -> "sourcenode")) ++ toMapBase

  override def addEdge(edgeId: String): Vertex = new SourceNode(id, edges + edgeId)
  override def delEdge(edgeId: String): Vertex = new SourceNode(id, edges - edgeId)
}

object SourceNode {
  def apply(id: String, edges: Set[String] = Set[String]()) = new SourceNode(id, edges)
  def apply(id: String, edgesStr: String) = new SourceNode(id, Vertex.str2iter(edgesStr).toSet)
}