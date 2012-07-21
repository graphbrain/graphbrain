package com.graphbrain.hgdb


trait UserOps extends VertexStoreInterface {

  def getOrCreate(id: String): Vertex = {
    if (exists(id)) {
      get(id)
    }
    else {
      val node = TextNode(id, id)
      super.put(node)
      node
    }
  }

  abstract override def put(vertex: Vertex): Vertex = {
    super.put(vertex)

    vertex match {
      case u: UserNode => {
        getOrCreate("user")
        addrel("sys/index", Array(vertex.id, "user"))
      }
      case t: TextNode => {
        if (ID.isInUserSpace(t.id)) {
          addrel("sys/owns", Array(ID.ownerId(t.id), t.id))
        }
        else {
          getOrCreate("global")
          addrel("sys/owns", Array("global", t.id)) 
        }
      }
      case u: URLNode => {
        if (ID.isInUserSpace(u.id)) {
          addrel("sys/owns", Array(ID.ownerId(u.id), u.id))
        }
        else {
          getOrCreate("global")
          addrel("sys/owns", Array("global", u.id)) 
        }
      }
      case _ => {}
    }

    vertex
  }

  def put2(vertex: Vertex, userid: String): Vertex = {
    if (!exists(vertex.id)) {
      put(vertex)
    }
    if (!ID.isInUserSpace(vertex.id)) {
      val userSpaceId = ID.globalToUser(vertex.id, userid)
      if (!exists(userSpaceId)) {
        put(vertex.clone(userSpaceId))
        addrel("sys/alt", Array(vertex.id, userSpaceId))
      }
    }

    vertex
  }

  def getOrInsert2(node:Vertex, userid: String): Vertex =
  {
    try {
      get(node.id)
    }
    catch {
      case _ => {
        put2(node, userid)
        get(node.id)
      }
    }
  }

  def addrel2(edgeType: String, participants: Array[String], userid: String) = {
    val userSpace = participants.exists(id => ID.isInUserSpace(id))

    if (!userSpace) {
      val ids = for (id <- participants) yield id
      addrel(edgeType.replace(" ", "_"), ids)
    }

    val ids = for (id <- participants) yield
      if (ID.isInUserSpace(id)) id else ID.globalToUser(id, userid)
    addrel(edgeType.replace(" ", "_"), ids)    
  }

  def delrel2(edgeType: String, participants: Array[String], userid: String): Unit = {
    val userSpaceParticipants = participants.map(p => ID.globalToUser(p, userid))
    delrel(edgeType, userSpaceParticipants)

    val globalSpaceParticipants = participants.map(p => ID.userToGlobal(p))
    delrel(edgeType, globalSpaceParticipants)
  }

  def delrel2(edgeId: String, userid: String): Unit = delrel2(Edge.edgeType(edgeId), Edge.participantIds(edgeId).toArray, userid)

  def createAndConnectVertices(edgeType: String, participants: Array[Vertex], userid: String) = {
    for (v <- participants) {
      put2(v, userid)
    }

    val ids = for (v <- participants) yield v.id
    addrel2(edgeType.replace(" ", "_"), ids, userid)
  }

  def removeAllEdges2(vertex: Vertex, userid: String) = {
    val nedges = neighborEdges(vertex.id)

    // remove connected edges
    for (edgeId <- nedges) {
      delrel2(edgeId, userid)
    }
  }
}