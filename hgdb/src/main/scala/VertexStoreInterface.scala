package com.graphbrain.hgdb

abstract trait VertexStoreInterface {
  def get(id: String): Vertex
  def put(vertex: Vertex): Vertex
  def update(vertex: Vertex): Vertex
  def remove(vertex: Vertex): Vertex
  def exists(id: String): Boolean
  def addrel(edgeType: String, participants: Array[String]): Boolean
  def delrel(edgeType: String, participants: Array[String]): Boolean
  def neighbors(nodeId: String): Set[(String, String)]
  def neighborEdges(nodeId: String): Set[String]

  def delrel(edgeId: String): Unit = delrel(Edge.edgeType(edgeId), Edge.participantIds(edgeId).toArray)

  def getOrNull(id: String): Vertex = {
    try {
      get(id)
    }
    catch {
      case e: KeyNotFound => null
    }
  }

  def getEdge(id: String): Edge = {
  	get(id) match {
  		case x: Edge => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'edg', found : '" + v.vtype + "')")
  	}
  }

  def getEdgeSet(id: String): EdgeSet = {
    get(id) match {
      case x: EdgeSet => x
      case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'edgs', found : '" + v.vtype + "')")
    }
  }

  def getExtraEdges(id: String): ExtraEdges = {
    get(id) match {
      case x: ExtraEdges => x
      case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'ext', found : '" + v.vtype + "')")
    }
  }

  def getExtraEdgesOrNull(id: String): ExtraEdges = {
    try {
      getExtraEdges(id)
    }
    catch {
      case e: KeyNotFound => null
    }
  }

  def getEdgeType(id: String): EdgeType = {
  	get(id) match {
  		case x: EdgeType => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'edgt', found : '" + v.vtype + "')")
  	}
  }

  def getTextNode(id: String): TextNode = {
  	get(id) match {
  		case x: TextNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'txt', found : '" + v.vtype + "')")
  	}
  }

  def getURLNode(id: String): URLNode = {
  	get(id) match {
  		case x: URLNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'url', found : '" + v.vtype + "')")
  	}
  }

  def getSourceNode(id: String): SourceNode = {
  	get(id) match {
  		case x: SourceNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'src', found : '" + v.vtype + "')")
  	}
  }

  def getUserNode(id: String): UserNode = {
    get(id) match {
      case x: UserNode => x
      case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'usr', found : '" + v.vtype + "')")
    }
  }

  def getUserEmailNode(id: String): UserEmailNode = {
    get(id) match {
      case x: UserEmailNode => x
      case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected 'usre', found : '" + v.vtype + "')")
    }
  }

  def createAndConnectVertices(edgeType: String, participants: Array[Vertex]) = {
    for (v <- participants) {
      if (!exists(v.id)) {
        put(v)
      }
    }

    val ids = for (v <- participants) yield v.id
    addrel(edgeType.replace(" ", "_"), ids)
  }

  def removeVertexAndEdges(vertex: Vertex) = {
    val nedges = neighborEdges(vertex.id)

    // remove connected edges
    for (edgeId <- nedges) {
      delrel(edgeId)
    }

    // remove vertex
    remove(vertex)
  }

  def nodeOwner(nodeId: String): String = {    
    val tokens = nodeId.split("/")
    if (tokens(0) == "user") {
      "user/" + tokens(1)  
    }
    else {
      ""
    }
  }
}