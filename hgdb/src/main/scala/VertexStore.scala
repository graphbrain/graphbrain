package com.graphbrain.hgdb


/** Vertex store.
  *
  * Implements and hypergraph database on top of a key/Map store. 
  */
class VertexStore(storeName: String, val maxEdges: Int=1000, ip: String="localhost", port: Int=9160) extends VertexStoreInterface {
  //val backend: Backend = new RiakBackend(storeName, ip, port)
  val backend: Backend = new CassandraBackend(storeName, ip, port)
}

object VertexStore {
  def apply(storeName: String) = new VertexStore(storeName)
}
