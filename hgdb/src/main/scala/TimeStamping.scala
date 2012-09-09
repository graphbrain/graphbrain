package com.graphbrain.hgdb

import java.util.Date


trait TimeStamping extends VertexStore {

  abstract override def put(vertex: Vertex): Vertex = {
    val v = if (vertex.ts == 0) {
      val curTs = (new Date()).getTime()
      vertex.setTs(curTs)
    }
    else {
      vertex
    }
    super.put(v)
    v
  }
}
