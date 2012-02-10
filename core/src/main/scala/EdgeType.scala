package com.graphbrain

class EdgeType(_id: String) extends Vertex(_id) {
  override val vtype = "edgeType"
}

object EdgeType {
  def apply(_id: String) = new EdgeType(_id) 
}