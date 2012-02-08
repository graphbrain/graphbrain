package com.graphbrain

class Edge(_id: String, links: List[String]) extends Vertex(_id, links) {
  override val vtype = "edge"
}

object Edge {
  def apply(_id: String, links: List[String] = List[String]()) = new Edge(_id, links) 
}