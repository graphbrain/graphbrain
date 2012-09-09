package com.graphbrain.hgdb

import scala.collection.mutable.Map

/** Aggregate writes to vertex store in bursts.
  *
  *  
  */
trait BurstCaching extends VertexStore {
  val readCache = Map[String, Vertex]()
  val writeCache = Map[String, Vertex]()

  abstract override def get(id: String): Vertex = {
    updateReadCache()
    if (readCache contains id) {
      readCache(id)  
    }
    else if (writeCache contains id) {
      writeCache(id)
    }
    else {
      readCache += (id -> super.get(id))
      readCache(id)
    }
  }

  abstract override def put(vertex: Vertex): Vertex = {
    updateWriteCache()
    readCache(vertex.id) = vertex
    writeCache(vertex.id) = vertex
    vertex
  }

  abstract override def update(vertex: Vertex): Vertex = {
    updateReadCache()
    updateWriteCache()
    readCache(vertex.id) = vertex
    writeCache(vertex.id) = vertex
    vertex
  }

  abstract override def remove(vertex: Vertex): Vertex = {
    readCache -= vertex.id
    writeCache -= vertex.id
    super.remove(vertex)
  }

  private def updateReadCache() = {
    if (readCache.size > 10000) {
      readCache.clear()
    }
  }

  def finish() = {
    writeCache foreach (item => super.put(item._2))
  }

  private def updateWriteCache() = {
    if (writeCache.size > 10000) {
      writeCache foreach (item => super.put(item._2))
      writeCache.clear()
    }
  }
}
