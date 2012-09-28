package com.graphbrain.hgdb


case class EdgeType(id: String="", label: String="", store: VertexStore=null) extends Vertex {

  override def clone(newid: String) = EdgeType(newid, label)
}