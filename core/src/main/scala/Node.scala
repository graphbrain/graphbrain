package com.graphbrain

class Node(_id: String, links: Set[String]) extends Vertex(_id, links) {
  override val vtype = "node"
}

object Node {
  def apply(_id: String, links: Set[String] = Set[String]()) = new Node(_id, links) 
}