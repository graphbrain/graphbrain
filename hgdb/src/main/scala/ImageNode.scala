package com.graphbrain.hgdb


class ImageNode(id: String, val url: String, edges: Set[String]) extends Node(id, edges) {
  override val vtype = "imagenode"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("url" -> url))

  override def toString: String = super.toString + "; url: " + url
}

object ImageNode {
  def apply(id: String, url: String, edges: Set[String] = Set[String]()) = new TextNode(id, url, edges)
  def apply(id: String, url: String, edgesStr: String) = new TextNode(id, url, Node.str2iter(edgesStr).toSet)
}