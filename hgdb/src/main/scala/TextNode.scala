package com.graphbrain.hgdb


case class TextNode(store: VertexStore, namespace: String="", text: String="", summary: String="") extends Textual {
  
  override val id = namespace + "/" + ID.sanitize(text).toLowerCase

  override def extendedId: String = namespace + "/" + ID.sanitize(text)

  override def put(): Vertex = {
    val template = if (ID.isInUserSpace(id)) store.backend.tpUserSpace else store.backend.tpGlobal
    val updater = template.createUpdater(id)
    updater.setString("text", text)
    updater.setString("summary", summary)
    template.update(updater)
    this
  }

  override def clone(newid: String) = TextNode(store, ID.namespace(newid), text, summary)

  override def toString: String = text

  override def updateSummary: Textual = TextNode(store, namespace, text, generateSummary)
}