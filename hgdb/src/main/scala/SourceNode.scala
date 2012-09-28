package com.graphbrain.hgdb


case class SourceNode(id: String="", store: VertexStore=null) extends Vertex {

  override def clone(newid: String) = SourceNode(newid)
}