package com.graphbrain.db

import scala.collection.mutable.{Set => MSet}

trait UserOps extends Graph {

  private def setOwner(userId: String, nodeId: String) = {
    ldebug("setOwner userId: " + userId + "; nodeId: " + nodeId)
    val col = HFactory.createColumn(nodeId, "", StringSerializer.get(), StringSerializer.get())
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addInsertion(userId, "owners", col)
    mutator.execute()
  }

  private def unsetOwner(userId: String, nodeId: String) = {
    ldebug("unsetOwner userId: " + userId + "; nodeId: " + nodeId)
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addDeletion(userId, "edges", nodeId, StringSerializer.get())
    mutator.execute()
  }

  private def linkToGlobal(globalNodeId: String, userNodeId: String) = {
    ldebug("linkToGlobal globalNodeId: " + globalNodeId + "; userNodeId: " + userNodeId)
    val col = HFactory.createColumn(userNodeId, "", StringSerializer.get(), StringSerializer.get())
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addInsertion(globalNodeId, "globaluser", col)
    mutator.execute()
  }

  private def unlinkToGlobal(globalNodeId: String, userNodeId: String) = {
    ldebug("unlinkToGlobal globalNodeId: " + globalNodeId + "; userNodeId: " + userNodeId)
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addDeletion(globalNodeId, "globaluser", userNodeId, StringSerializer.get())
    mutator.execute()
  }

  override def onPut(vertex: Vertex) = {
    ldebug("onPut " + vertex.id)
    vertex match {
      case t: TextNode =>
        if (ID.isInUserSpace(t.id))
          setOwner(ID.ownerId(t.id), t.id)

      case u: URLNode =>
        if (ID.isInUserSpace(u.id))
          setOwner(ID.ownerId(u.id), u.id)

      case c: ContextNode =>
        if (ID.isInUserSpace(c.id))
          setOwner(ID.ownerId(c.id), c.id)

      case _ =>
    }

    super.onPut(vertex)
  }

  override def remove(vertex: Vertex): Vertex = {
    ldebug("remove " + vertex)
    super.remove(vertex)

    val extraStuff = vertex match {
      case t: TextNode => true
      case u: URLNode => true
      case _ => false
    }

    if (extraStuff) {
      val id = vertex.id
      if (ID.isInUserSpace(id)) {
        unsetOwner(ID.ownerId(id), id)
        if (!ID.isPersonal(id)) {
          unlinkToGlobal(ID.userToGlobal(id), id)
        }
      }
      else {
        backend.tpGlobalUser.deleteRow(id)
      }
    }

    vertex
  }

  def put2(vertex: Vertex, userid: String): Vertex = {
    ldebug("put2 " + vertex + "; userId: " + userid)
    if (vertex.shouldUpdate) {
      ldebug("should update " + vertex)
      put(vertex)
    }
    if (!ID.isInUserSpace(vertex.id)) {
      val userSpaceId = ID.globalToUser(vertex.id, userid)
      if (!exists(userSpaceId)) {
        put(get(vertex.id).toUser(userid))
        linkToGlobal(vertex.id, userSpaceId)
      }
    }

    get(vertex.id)
  }

  def getOrInsert2(node: Vertex, userid: String): Vertex =
  {
    ldebug("getOrInsert2 " + node + "; userId: " + userid)
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
    ldebug("addrel2 edgeType: " + edgeType + "; participants: " + participants + "; userid: " + userid + "; consensus: " + consensus)

    val etype = edgeType.replace(" ", "_")

    // convert edge to user space and add
    val ids = for (id <- participants) yield
      if (ID.isInUserSpace(id)) id else ID.globalToUser(id, userid)
    addrel(etype, ids.toList)

    val edge = Edge(etype, ids.toList)
    
    if (!edge.isInContextSpace) {
      // remove negation of this edge if it exists
      delrel(edge.negate)

      // run consensus algorithm
      if (consensus) {
        val gids = for (id <- participants) yield
          if (!ID.isInUserSpace(id)) id else ID.userToGlobal(id)
        Consensus.evalEdge(Edge(edgeType, gids.toList), this)
      }
    }
  }

  def delrel2(edgeType: String, participants: List[String], userid: String, consensus: Boolean=false): Unit = {
    ldebug("delrel2 edgeType: " + edgeType + "; participants: " + participants + "; userid: " + userid + "; consensus: " + consensus)
    
    val edge = Edge(edgeType, participants.toList)
    val context = edge.isInContextSpace

    // delete edge from user space
    val userSpaceParticipants = if (context) participants else participants.map(p => ID.globalToUser(p, userid))
    delrel(edgeType, userSpaceParticipants.toList)

    if (!context) {
      // create negation of edge in user space
      addrel(Edge(edgeType, userSpaceParticipants.toList).negate)

      // run consensus algorithm
      if (consensus) {
        val gids = for (id <- participants) yield
          if (!ID.isInUserSpace(id)) id else ID.userToGlobal(id)
        Consensus.evalEdge(Edge(edgeType, gids.toList), this)
      }
    }
  }

  def delrel2(edge: Edge, userid: String): Unit = delrel2(edge.edgeType, edge.participantIds, userid)

  def createAndConnectVertices2(edgeType: String, participants: Array[Vertex], userid: String, consensus: Boolean = false) = {
    ldebug("createAndConnectVertices2 edgeType: " + edgeType + "; participants: " + participants + "; userid: " + userid + "; consensus: " + consensus)
    for (v <- participants) {
      put2(v, userid)
    }

    val ids = for (v <- participants) yield v.id
    addrel2(edgeType.replace(" ", "_"), ids, userid, consensus)
  }

  def neighborEdges2(nodeId: String, userid: String, edgeType: String = "", relPos: Integer = -1): Set[Edge] = {
    ldebug("neighborEdges2 nodeId: " + nodeId + "; userid: " + userid + "; edgeType: " + edgeType + "; pos: " + relPos)
    
    // context space
    if (ID.isInContextSpace(nodeId)) {
      neighborEdges(nodeId, edgeType, relPos).filter(x => x.isInContextSpace)
    }
    // global space
    else {
      val uNodeId = ID.globalToUser(nodeId, userid) 

      val gedges = neighborEdges(nodeId, edgeType, relPos).filter(x => x.isGlobal)
      val uedges = neighborEdges(uNodeId, edgeType, relPos).filter(x => x.isInUserSpace).map(x => x.toGlobal)

      val gnhood = nodesFromEdgeSet(gedges)
      val unhood = nodesFromEdgeSet(uedges)

      val applyNegatives = gedges.filter(x => !uedges.contains(x.negate))
      val posUEdges = uedges.filter(x => x.isPositive)

      applyNegatives ++ posUEdges
    }
  }

  def globalAlts(globalId: String) = {
    ldebug("globalAlts: " + globalId)
    val altSet = MSet[String]()

    val query = HFactory.createSliceQuery(backend.ksp, StringSerializer.get(),
      StringSerializer.get(), StringSerializer.get()).setKey(globalId).setColumnFamily("globaluser")

    val iterator = new ColumnSliceIterator[String, String, String](query, null, "\uFFFF", false);

    while (iterator.hasNext) {
      val column = iterator.next
      altSet += column.getName
    }

    altSet.toSet
  }

  def createContext(name: String, userid: String, access: String) = {
    ldebug("createContext: " + name + "; user: " + userid + "; access: " + access)
    val contextNode = ContextNode(this, userid, name, access)

    val userNode = getUserNode(userid)
    val contexts = if (userNode.contexts == null) List[ContextNode]() else userNode.contexts

    // check if already exists
    if (!contexts.exists(_.id == contextNode.id)) {
      ldebug("context does not exist yet")
      // add to user node
      val newContexts = contextNode :: contexts
      put(userNode.copy(contexts = newContexts))

      // add context node
      put(contextNode)
    }
    else {
      ldebug("context already exists")
    }
  }
}