package com.graphbrain.hgdb


class Edges(val vertexId: String, val pos: Int, val rel: String, val store: VertexStore) extends Iterable[String] {
  val edgeSetId = vertexId + "/" + pos + "/" + rel

  override def iterator: Iterator[String] = new EdgeIterator(edgeSetId, store)
}
