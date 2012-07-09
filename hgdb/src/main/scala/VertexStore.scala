package com.graphbrain.hgdb


/** Vertex store.
  *
  * Implements and hypergraph database on top of a key/Map store. 
  */
class VertexStore(storeName: String, val maxEdges: Int=1000, ip: String="127.0.0.1", port: Int=8098) extends VertexStoreInterface {
  val backend: Backend = new RiakBackend(storeName, ip, port)
}

object VertexStore {
  def apply(storeName: String) = new VertexStore(storeName)
}