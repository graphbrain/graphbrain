package com.graphbrain.hgdb


trait UserOps extends VertexStoreInterface {

  def createAndConnectVertices(edgeType: String, participants: Array[Vertex], userid: String) = {
    for (v <- participants) {
      if (!exists(v.id)) {
        put(v)
      }
    }

    val ids = for (v <- participants) yield v.id
    addrel(edgeType.replace(" ", "_"), ids)
  }
}