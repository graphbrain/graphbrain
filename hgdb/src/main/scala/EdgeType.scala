package com.graphbrain.hgdb


case class EdgeType(store: VertexStore, id: String="", label: String="") extends Vertex {

  override def put(): Vertex = {
    val template = store.backend.tpEdgeType
    val updater = template.createUpdater(id)
    updater.setString("label", label)
    template.update(updater)
    store.onPut(this)
    this
  }

  override def clone(newid: String) = EdgeType(store, newid, label)
}