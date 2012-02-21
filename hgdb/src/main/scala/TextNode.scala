package com.graphbrain.hgdb


class TextNode(id: String, val text: String, edges: Set[String]) extends Node(id, edges) {
  override val vtype = "textnode"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("text" -> text))

  override def toString: String = super.toString + "; text: " + text
}

object TextNode {
  def apply(id: String, text: String, edges: Set[String] = Set[String]()) = new TextNode(id, text, edges)
  def apply(id: String, text: String, edgesStr: String) = new TextNode(id, text, Vertex.str2iter(edgesStr).toSet)
}