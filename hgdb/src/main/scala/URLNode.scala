package com.graphbrain.hgdb


class URLNode(id: String, val url: String, edges: Set[String]) extends Node(id, edges) {
  override val vtype = "urlnode"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("url" -> url))

  override def toString: String = super.toString + "; url: " + url
}

object URLNode {
  def apply(id: String, url: String, edges: Set[String] = Set[String]()) = new TextNode(id, url, edges)
  def apply(id: String, url: String, edgesStr: String) = new TextNode(id, url, Vertex.str2iter(edgesStr).toSet)
}