package com.graphbrain.hgdb


case class SourceNode(id: String="") extends Vertex {

  override def clone(newid: String) = SourceNode(newid)
}