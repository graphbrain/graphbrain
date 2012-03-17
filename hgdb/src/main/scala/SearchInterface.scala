package com.graphbrain.hgdb


abstract trait SearchInterface {
  def initIndex(): Unit
  def index(key: String, text: String): Unit
  def query(text: String): SearchResults

  def indexVertex(vertex: Vertex) = {
    vertex match {
      case t: TextNode => index(t.id, t.text)
      case _ =>
    }
  }
}