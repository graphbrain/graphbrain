package com.graphbrain.db

case class TextNode(namespace: String="", text: String="", summary: String="") extends Textual {
  
  override val id = namespace + "/" + ID.sanitize(text).toLowerCase

  override def vtype = VertexType.Text

  override def extraMap = Map("text" -> text, "summary" -> summary)

  override def clone(newid: String) = TextNode(ID.namespace(newid), text, summary)

  override def toGlobal: Vertex = TextNode(ID.userToGlobal(namespace), text, summary)

  override def toUser(newUserId: String): Vertex = TextNode(ID.globalToUser(namespace, newUserId), text, summary)

  override def removeContext() = TextNode(ID.removeContext(namespace), text, summary)

  override def setContext(newContext: String): Vertex = TextNode(ID.setContext(namespace, newContext), text, summary)

  override def toString: String = text

  /*
  override def updateSummary: Textual = TextNode(namespace, text, generateSummary)


  override def updateFromEdges(): Vertex = {
    val newSummary = generateSummary
    val newVertex = this.copy(summary=newSummary)

    if (this != newVertex) {
      put()
    }

    this
  }*/

  override def raw: String = {
    "type: " + "text<br />" +
    "text: " + text + "<br />" +
    "summary: " + summary + "<br />"
  }
}


object TextNode {
  def fromId(id: String) = {
    val namespace = ID.namespace(id)
    val text = ID.humanReadable(id) 
    TextNode(namespace = namespace, text = text, summary = "")
  }
}