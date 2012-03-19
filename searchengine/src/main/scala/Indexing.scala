package com.graphbrain.searchengine

import scala.collection.mutable.Map
import com.graphbrain.hgdb.VertexStoreInterface
import com.graphbrain.hgdb.Vertex

/** Indexes vertices on put.
  */
trait Indexing extends VertexStoreInterface {
  val si = RiakSearchInterface("gbsearch")
  si.initIndex()

  abstract override def get(id: String): Vertex = super.get(id)

  abstract override def put(vertex: Vertex): Vertex = {
    si.indexVertex(vertex)
    super.put(vertex)
  }

  abstract override def update(vertex: Vertex): Vertex = {
    super.update(vertex)
  }

  abstract override def remove(vertex: Vertex): Vertex = {
    super.remove(vertex)
  }
}