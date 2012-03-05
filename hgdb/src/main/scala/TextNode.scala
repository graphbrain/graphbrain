package com.graphbrain.hgdb


class TextNode(id: String, val text: String, edges: Set[String]) extends Node(id, edges) {
  override def toMap: Map[String, Any] = Map(("vtype" -> "textnode")) ++ toMapBase
  override def toMapBase: Map[String, Any] = super.toMapBase ++ Map(("text" -> text))

  override def addEdge(edgeId: String): Vertex = new TextNode(id, text, edges + edgeId)
  override def delEdge(edgeId: String): Vertex = new TextNode(id, text, edges - edgeId)

  override def toString: String = "textnode " + super.toString + "; text: " + text
}

object TextNode {
  def apply(id: String, text: String, edges: Set[String] = Set[String]()) = new TextNode(id, text, edges)
  def apply(id: String, text: String, edgesStr: String) = new TextNode(id, text, Vertex.str2iter(edgesStr).toSet)
}