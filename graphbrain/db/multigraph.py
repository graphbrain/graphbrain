package com.graphbrain.db;

public class MultiGraph extends Graph {

	private void linkToGlobal(String globalNodeId, String userNodeId) {
    
	}

	private void unlinkToGlobal(String globalNodeId, String userNodeId) {
    
	}

	@Override
	public void remove(Vertex vertex) {
		//ldebug("remove " + vertex)
		super.remove(vertex);

		boolean extraStuff = false;
	  
		switch(vertex.type()) {
		case Text:
			extraStuff = true;
			break;
		case URL:
			extraStuff = true;
			break;
		}

		if (extraStuff) {
			String id = vertex.getId();
			if (!ID.isPersonal(id)) {
				unlinkToGlobal(ID.userToGlobal(id), id);
			}
		}
	}

	public Vertex put2(Vertex vertex, String userid) {
		//ldebug("put2 " + vertex + "; userId: " + userid)
		if (vertex.shouldUpdate()) {
			//ldebug("should update " + vertex)
			put(vertex);
		}
		if (!ID.isInUserSpace(vertex.id)) {
			String userSpaceId = ID.globalToUser(vertex.getId(), userid);
			if (!exists(userSpaceId)) {
				put(get(vertex.id).toUser(userid));
				linkToGlobal(vertex.id, userSpaceId);
			}
		}

		return get(vertex.getId());
	}

	public Vertex getOrInsert2(Vertex node, String userid) {
		//ldebug("getOrInsert2 " + node + "; userId: " + userid)
    
		Vertex n = get(node.getId());
		Vertex un = get(ID.globalToUser(node.getId(), userid));
    
		if (n == null || un == null) {
			put2(node, userid);
			n = get(node.getId());
		}
		
		return n;
	}

	public void addrel2(String edgeType, String[] participants, String userid, boolean consensus/*=false*/) {
		//ldebug("addrel2 edgeType: " + edgeType + "; participants: " + participants + "; userid: " + userid + "; consensus: " + consensus)

		String etype = edgeType.replace(" ", "_");

		// convert edge to user space and add
		String[] ids = new String[participants.length];
		for (int i = 0; i < participants.length; i++) {
			String id = participants[i];
			if (ID.isInUserSpace(id)) {
				ids[i] = id;
			}
			else {
				ids[i] = ID.globalToUser(id, userid);
			}
		}
		addrel(etype, ids.toList);

		Edge edge = new Edge(etype, ids);
    
		if (!edge.isInContextSpace()) {
			// remove negation of this edge if it exists
			delrel(edge.negate());

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