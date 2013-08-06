package com.graphbrain.gbdb

import scala.collection.mutable.Map

/** Simple cache for a vertex store.
  *
  * Implements an in-memory write-through cache for vertices that
  * grows unboundedly. 
  */
trait SimpleCaching extends VertexStore {
  val cache = Map[String, Vertex]()

  abstract override def get(id: String): Vertex = {
    if (!(cache contains id))
      cache += (id -> super.get(id))
    cache(id)
  }

  override def onPut(vertex: Vertex) = {
    cache(vertex.id) = vertex
    super.onPut(vertex)
  }

  abstract override def update(vertex: Vertex): Vertex = {
    cache(vertex.id) = vertex
    super.update(vertex)
  }

  abstract override def remove(vertex: Vertex): Vertex = {
    cache -= vertex.id
    super.remove(vertex)
  }

  def clear(limit: Int=99999) = {
    if (cache.size > limit) {
      cache.clear()
    }
  }
}
