package com.graphbrain.db;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Graph {

	private Backend back;
	
	public Graph() {
		back = new LevelDbBackend();
	}
	
	public Vertex get(String id) {
		return back.get(id, VertexType.type(id));
	}
	
	public Vertex put(Vertex vertex) {
		back.put(vertex);
		
		if (vertex.type() == VertexType.Edge) {
			onPutEdge((Edge)vertex);
		}
		
		return vertex;
	}
	
	public Vertex update(Vertex vertex) {
		return back.update(vertex);
	}
	
	public void remove(Vertex vertex) {
		back.remove(vertex);
		
		if (vertex.type() == VertexType.Edge) {
			onRemoveEdge((Edge)vertex);
		}
	}
	
	public boolean exists(String id) {
		return get(id) != null;
	}
	
	public UserNode getUserNode(String id) {
		return (UserNode)back.get(id, VertexType.User);
	}
	
	private String idFromEmail(String email) {
		String userName = back.usernameByEmail(email);
		if (userName == null) {
			return null;
		}
		else {
			return ID.idFromUsername(userName);
		}
	}
	
	public boolean usernameExists(String username) {
		return exists(ID.idFromUsername(username));
	}
	
	public boolean emailExists(String email) {
		String userName = back.usernameByEmail(email);
	    return userName != null; 
	}
	
	public UserNode findUser(String login) {
		if (exists(ID.idFromUsername(login))) {
		  	  return getUserNode(ID.idFromUsername(login));
		}
		else {
			String uid = idFromEmail(login);
		    if (uid == null) {
		    	return null;
		    }
		    else if (exists(uid)) {
		  	    return getUserNode(idFromEmail(login));
		    }
		    else {
		        return null;
		    }
		}
	}
	
	public UserNode getUserNodeByUsername(String username) {
		if (exists(ID.idFromUsername(username))) {
			return getUserNode(ID.idFromUsername(username));
		}
		else {
			return null;
		}
	}
	
	public UserNode createUser(String username, String name, String email, String password, String role) {
		UserNode userNode = UserNode.create(username, name, email, password, role);
		back.put(userNode);
		if (!email.equals("")) {
		    back.associateEmailToUsername(email, username);
		}

		return userNode;
	}
	
	public UserNode attemptLogin(String login, String password) {
		UserNode userNode = findUser(login);

		// user does not exist
		if (userNode == null) {
			return null;
		}
		    
		// password is incorrect
		if (!userNode.checkPassword(password)) {
			return null;
		}

		// ok, create new session
		userNode.newSession();
		back.update(userNode);
		return userNode;
	}
	
	public UserNode forceLogin(String login) {
		UserNode userNode = findUser(login);

		// user does not exist
		if (userNode == null) {
			return null;
		}

		// ok, create new session
		userNode.newSession();
		back.update(userNode);
		return userNode;
	}
	
	public List<Vertex> allUsers() {
		return back.listByType(VertexType.User);
	}
	
	public Set<Edge> edges(Vertex center) {
		return back.edges(center);
	}
	
	public Set<Edge> edges(String centerId) {
		Vertex center = get(centerId);
		return edges(center);
	}
	
	public Set<String> nodesFromEdgeSet(Set<Edge> edgeSet) {
		Set<String> nset = new HashSet<String>();

		for (Edge e : edgeSet) {
			for (String pid : e.getIds()) {
				nset.add(pid);
		    }
		}
		
		return nset;
	}

	public Set<String> neighbors(String centerId) {
		//ldebug("neighbors " + nodeId)

		Set<Edge> nedges = edges(centerId);
		Set<String> nodes = nodesFromEdgeSet(nedges);
		nodes.add(centerId);
		return nodes;
	}
	
	protected void incDegree(Vertex vertex) {
		vertex.incDegree();
		update(vertex);
	}
	
	protected void incDegree(String id) {
		Vertex vertex = get(id);
		incDegree(vertex);
	}
	
	protected void decDegree(Vertex vertex) {
		vertex.decDegree();
		update(vertex);
	}
	
	protected void decDegree(String id) {
		Vertex vertex = get(id);
		decDegree(vertex);
	}
	
	protected void onPutEdge(Edge edge) {
		for (String id : edge.getIds()) {
			incDegree(id);
		}
	}
	
	protected void onRemoveEdge(Edge edge) {
		for (String id : edge.getIds()) {
			decDegree(id);
		}
	}

/*

  def addrel(edge: Edge): Edge = {
    ldebug("addrel " + edge)

    if (!relExists(edge)) {
      incInstances(edge.edgeType)
      for (i <- 0 until edge.participantIds.size) {
        val p = edge.participantIds(i)
        addEdgeEntry(p, edge)
        incVertexEdgeType(p, edge.edgeType, i)
        incDegree(p)
      }
    }

    edge
  }


  def addrel(edgeType: String, participants: List[String]): Edge = addrel(Edge(edgeType, participants))


  def delrel(edge: Edge): Unit = {
    ldebug("delrel " + edge)

    if (relExists(edge)) {
      decInstances(edge.edgeType)
      for (i <- 0 until edge.participantIds.size) {
        val p = edge.participantIds(i)
        delEdgeEntry(p, edge)
        decVertexEdgeType(p, edge.edgeType, i)
        decDegree(p)
      }
    }
  }


  def delrel(edgeType: String, participants: List[String]): Unit = delrel(Edge(edgeType, participants))


  def createAndConnectVertices(edgeType: String, participants: Array[Vertex]) = {
    ldebug("createAndConnectVertices edgeType: " + edgeType + "; participants: " + participants)
    for (v <- participants) {
      if (!exists(v.id)) {
        put(v)
      }
    }

    val ids = for (v <- participants) yield v.id
    addrel(edgeType.replace(" ", "_"), ids.toList)
  }
  */
}