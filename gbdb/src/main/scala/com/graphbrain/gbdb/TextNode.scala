package com.graphbrain.gbdb


case class TextNode(store: VertexStore, namespace: String="", text: String="", summary: String="") extends Textual {
  
  override val id = namespace + "/" + ID.sanitize(text).toLowerCase

  override def extendedId: String = namespace + "/" + ID.sanitize(text)

  override def put(): Vertex = {
    val template = if (ID.isInUserSpace(id)) store.backend.tpUserSpace else store.backend.tpGlobal
    val updater = template.createUpdater(id)
    updater.setString("text", text)
    if ((summary != "") && (summary != null))
      updater.setString("summary", summary)
    template.update(updater)
    store.onPut(this)
    this
  }

  override def clone(newid: String) = TextNode(store, ID.namespace(newid), text, summary)

  override def toGlobal: Vertex = TextNode(store, ID.userToGlobal(namespace), text, summary)

  override def toUser(newUserId: String): Vertex = TextNode(store, ID.globalToUser(namespace, newUserId), text, summary)

  override def removeContext: Vertex = TextNode(store, ID.removeContext(namespace), text, summary)

  override def setContext(newContext: String): Vertex = TextNode(store, ID.setContext(namespace, newContext), text, summary)

  override def toString: String = text

  override def updateSummary: Textual = TextNode(store, namespace, text, generateSummary)


  override def updateFromEdges(): Vertex = {
    val newSummary = generateSummary
    val newVertex = this.copy(summary=newSummary)

    if (this != newVertex) {
      put()
    }

    this
  }

  override def raw: String = {
    "type: " + "text<br />" +
    "text: " + text + "<br />" +
    "summary: " + summary + "<br />"
  }
}


object TextNode {
  def fromId(id: String, store: VertexStore) = {
    val namespace = ID.namespace(id)
    val text = ID.humanReadable(id) 
    TextNode(store, namespace, text, "")
  }
}