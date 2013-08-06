package com.graphbrain.gbdb


case class SourceNode(store: VertexStore, id: String="") extends Vertex {

  override def put(): Vertex = {
    val template = store.backend.tpGlobal
    val updater = template.createUpdater(id)
    template.update(updater)
    store.onPut(this)
    this
  }

  override def clone(newid: String) = SourceNode(store, newid)
}