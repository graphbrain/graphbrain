package com.graphbrain.hgdb

import scala.collection.mutable.Map

/** Simple cache for a vertex store.
  *
  * Implements an in-memory write-through cache for vertices that
  * grows unboundedly. 
  */
trait SimpleCaching extends VertexStoreInterface {
  val cache = Map[String, Vertex]()

  abstract override def get(id: String): Vertex = {
    if (!(cache contains id))
      cache += (id -> super.get(id))
    cache(id)
  }

  abstract override def put(vertex: Vertex): Vertex = {
    cache(vertex.id) = vertex
    super.put(vertex)
  }

  abstract override def update(vertex: Vertex): Vertex = {
    cache(vertex.id) = vertex
    super.update(vertex)
  }

  abstract override def remove(vertex: Vertex): Vertex = {
    cache -= vertex.id
    super.remove(vertex)
  } 
}