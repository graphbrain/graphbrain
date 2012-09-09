package com.graphbrain.hgdb

import scala.collection.mutable.Map

/** Print log of operations performed on store.
  *
  * Useful for debugging. 
  */
trait OpLogging extends VertexStore {
  abstract override def get(id: String): Vertex = {
    println("[get] " + id)
    super.get(id)
  }

  abstract override def put(vertex: Vertex): Vertex = {
    println("[put] " + vertex.id)
    super.put(vertex)
  }

  abstract override def update(vertex: Vertex): Vertex = {
    println("[update] " + vertex.id)
    super.update(vertex)
  }

  abstract override def remove(vertex: Vertex): Vertex = {
    println("[remove] " + vertex.id)
    super.remove(vertex)
  } 
}
