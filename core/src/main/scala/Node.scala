package com.graphbrain

class Node(_id: String, val links: Set[String]) extends Vertex(_id) {
  override val vtype = "node"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("links" -> links))
}

object Node {
  def apply(_id: String, links: Set[String] = Set[String]()) = new Node(_id, links) 
}