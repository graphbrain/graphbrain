package com.graphbrain.hgdb


case class TextNode(namespace: String="", text: String="") extends Vertex {
  
  override val id = namespace + "/" + ID.sanitize(text).toLowerCase

  override def extendedId: String = namespace + "/" + ID.sanitize(text)

  override def clone(newid: String) = TextNode(ID.namespace(newid), text)

  override def toString: String = text
}