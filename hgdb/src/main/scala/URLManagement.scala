package com.graphbrain.hgdb


trait URLManagement extends VertexStoreInterface {
  abstract override def put(vertex: Vertex): Vertex = {
    vertex match {
      case u: URLNode => {
        super.put(u)
      }
      case v => super.put(v)
    }
  } 
}