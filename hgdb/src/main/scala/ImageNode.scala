package com.graphbrain.hgdb


class ImageNode(id: String, val url: String, edges: Set[String]) extends Node(id, edges) {
  override def toMap: Map[String, Any] = Map(("vtype" -> "imagenode")) ++ toMapBase
  override def toMapBase: Map[String, Any] = super.toMapBase ++ Map(("url" -> url))

  override def addEdge(edgeId: String): Vertex = new ImageNode(id, url, edges + edgeId)
  override def delEdge(edgeId: String): Vertex = new ImageNode(id, url, edges - edgeId)

  override def toString: String = super.toString + "; url: " + url
}

object ImageNode {
  def apply(id: String, url: String, edges: Set[String] = Set[String]()) = new TextNode(id, url, edges)
  def apply(id: String, url: String, edgesStr: String) = new TextNode(id, url, Vertex.str2iter(edgesStr).toSet)
}