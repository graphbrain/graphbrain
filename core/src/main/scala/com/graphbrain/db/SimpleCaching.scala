package com.graphbrain.db

import scala.collection.mutable

trait SimpleCaching extends Graph {
  val cache = mutable.Map[String, Vertex]()

  abstract override def get(id: String): Vertex = {
    if (!(cache contains id))
      cache += (id -> super.get(id))
    cache(id)
  }

  override def put(vertex: Vertex) = {
    cache(vertex.id) = vertex
    super.put(vertex)
  }

  abstract override def update(vertex: Vertex): Vertex = {
    cache(vertex.id) = vertex
    super.update(vertex)
  }

  abstract override def remove(vertex: Vertex) = {
    cache -= vertex.id
    super.remove(vertex)
  }

  def clear(limit: Int=99999) = {
    if (cache.size > limit) {
      cache.clear()
    }
  }
}