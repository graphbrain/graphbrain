package com.graphbrain.hgdb


trait UserOps extends VertexStoreInterface {

  def createAndConnectVertices(edgeType: String, participants: Array[Vertex], userid: String) = {
    val userSpace = participants.exists(v => ID.isInUserSpace(v.id))

    for (v <- participants) {
      if (!exists(v.id)) {
        put(v)
      }
      if (!ID.isInUserSpace(v.id)) {
      	val userSpaceId = ID.globalToUser(v.id, userid)
      	if (!exists(userSpaceId)) {
          put(v.clone(userSpaceId))
      	}	
      }
    }

    if (!userSpace) {
      val ids = for (v <- participants) yield v.id
      addrel(edgeType.replace(" ", "_"), ids)
    }

    val ids = for (v <- participants) yield
      if (ID.isInUserSpace(v.id)) v.id else ID.globalToUser(v.id, userid)
    addrel(edgeType.replace(" ", "_"), ids)
  }
}