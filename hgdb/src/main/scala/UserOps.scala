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
        put(get(vertex.id).clone(userSpaceId))
        addrel("sys/alt", Array(vertex.id, userSpaceId))
      }
    }

    get(vertex.id)
  }

  def getOrInsert2(node:Vertex, userid: String): Vertex =
  {
    try {
      get(node.id)
      get(ID.globalToUser(node.id, userid))
    }
    catch {
      case _ => {
        put2(node, userid)
        get(node.id)
      }
    }
  }

  def addrel2(edgeType: String, participants: Array[String], userid: String, consensus: Boolean=false) = {
    val etype = edgeType.replace(" ", "_")

    // convert edge to user space and add
    val ids = for (id <- participants) yield
      if (ID.isInUserSpace(id)) id else ID.globalToUser(id, userid)
    addrel(etype, ids)

    // remove negation of this edge if it exists
    delrel(ID.negateEdge(etype), ids)

    // run consensus algorithm
    if (consensus) {
      val gids = for (id <- participants) yield
        if (!ID.isInUserSpace(id)) id else ID.userToGlobal(id)
      Consensus.evalEdge(ID.edgeId(edgeType, gids), this)
    }
  }

  def delrel2(edgeType: String, participants: Array[String], userid: String, consensus: Boolean=false): Unit = {
    // delete edge from user space
    val userSpaceParticipants = participants.map(p => ID.globalToUser(p, userid))
    delrel(edgeType, userSpaceParticipants)

    // create negation of edge in user space
    addrel(ID.negateEdge(edgeType), userSpaceParticipants)

    // run consensus algorithm
    if (consensus) {
      val gids = for (id <- participants) yield
        if (!ID.isInUserSpace(id)) id else ID.userToGlobal(id)
      Consensus.evalEdge(ID.edgeId(edgeType, gids), this)
    }
  }

  def delrel2(edgeId: String, userid: String): Unit = delrel2(Edge.edgeType(edgeId), Edge.participantIds(edgeId).toArray, userid)

  def createAndConnectVertices2(edgeType: String, participants: Array[Vertex], userid: String, consensus: Boolean = false) = {
    for (v <- participants) {
      put2(v, userid)
    }

    val ids = for (v <- participants) yield v.id
    addrel2(edgeType.replace(" ", "_"), ids, userid, consensus)
  }

  def neighborEdges2(nodeId: String, userid: String): Set[String] = {
    val uNodeId = ID.globalToUser(nodeId, userid) 

    val gnhood = neighbors(nodeId).filter(x => ID.isUserNode(x) || (!ID.isInUserSpace(x)))
    val unhood = neighbors(uNodeId).filter(x => ID.isUserNode(x) || ID.isInUserSpace(x))

    val gedges = neighborEdges(nodeId, gnhood)
    val uedges = neighborEdges(uNodeId, unhood).map(ID.userToGlobalEdge)

    val applyNegatives = gedges.filter(x => !uedges.contains(ID.negateEdge(x)))
    val posUEdges = uedges.filter(x => ID.isPositiveEdge(x))

    applyNegatives ++ posUEdges
  }
}
