package com.graphbrain.searchengine

import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.TextNode


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