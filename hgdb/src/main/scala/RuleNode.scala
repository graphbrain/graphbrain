package com.graphbrain.hgdb


//To store the rule body
case class RuleNode(store: VertexStore, id: String="", rule: String="") extends Vertex {
  
  override def put(): Vertex = {
    val template = store.backend.tpGlobal
    val updater = template.createUpdater(id)
    updater.setString("rule", rule)
    template.update(updater)
    this
  }

  override def clone(newid: String) = RuleNode(store, newid, rule)

  override def toString: String = rule
}