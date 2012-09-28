package com.graphbrain.hgdb


case class TextNode(namespace: String="", text: String="", summary: String="", store: VertexStore=null) extends Textual {
  
  override val id = namespace + "/" + ID.sanitize(text).toLowerCase

  override def extendedId: String = namespace + "/" + ID.sanitize(text)

  override def clone(newid: String) = TextNode(ID.namespace(newid), text, summary, store)

  override def toString: String = text

  override def updateSummary: Textual = TextNode(namespace, text, generateSummary, store)
}