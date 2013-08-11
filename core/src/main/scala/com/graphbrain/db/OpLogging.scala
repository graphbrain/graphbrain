package com.graphbrain.db

trait OpLogging extends Graph {
  abstract override def get(id: String): Vertex = {
    println("[get] " + id)
    super.get(id)
  }

  abstract override def put(vertex: Vertex) = {
    println("[put] " + vertex.id)
    super.put(vertex)
  }

  abstract override def update(vertex: Vertex): Vertex = {
    println("[update] " + vertex.id)
    super.update(vertex)
  }

  abstract override def remove(vertex: Vertex) = {
    println("[remove] " + vertex.id)
    super.remove(vertex)
  } 
}
