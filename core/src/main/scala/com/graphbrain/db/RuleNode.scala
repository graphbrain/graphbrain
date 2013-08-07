package com.graphbrain.db


//To store the rule body
case class RuleNode(store: VertexStore, id: String="", rule: String="") extends Vertex {
  
  override def put(): Vertex = {
    val template = store.backend.tpGlobal
    val updater = template.createUpdater(id)
    updater.setString("rule", rule)
    template.update(updater)
    store.onPut(this)
    this
  }

  override def clone(newid: String) = RuleNode(store, newid, rule)

  override def toString: String = rule
}