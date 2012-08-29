package com.graphbrain.hgdb

import scala.collection.mutable.{Set => MSet}


abstract trait VertexStoreInterface {
  val backend: Backend
  val maxEdges: Int

  private def str2iter(str: String) = {
    (for (str <- str.split(',') if str != "")
      yield str.replace("$2", ",").replace("$1", "$")).toIterable
  }

  /** Gets Vertex by it's id */
  def get(id: String): Vertex = {
    val map = backend.get(id)
    val edgesets = str2iter(map.getOrElse("edgesets", "").toString).toSet
    val vtype = map.getOrElse("vtype", "")
    val degree = map.getOrElse("degree", "0").toString.toInt
    val ts = map.getOrElse("ts", "0").toString.toLong
    vtype match {
      case "edg" => {
        val etype = map.getOrElse("etype", "").toString
        Edge(id, etype, edgesets, degree, ts)
      }
      case "edgs" => {
        val edges = str2iter(map.getOrElse("edges", "").toString).toSet
        val extra = map.getOrElse("extra", "-1").toString.toInt
        val size = map.getOrElse("size", "0").toString.toInt
        EdgeSet(id, edges, extra, size) 
      }
      case "ext" => {
        val edges = str2iter(map.getOrElse("edges", "").toString).toSet
        ExtraEdges(id, edges)
      }
      case "edgt" => {
        val label = map.getOrElse("label", "").toString
        val instances = map.getOrElse("instances", "0").toString.toInt
        EdgeType(id, label, edgesets, instances, degree, ts)
      }
      case "txt" => {
        val text = map.getOrElse("text", "").toString
        TextNode(ID.namespace(id), text, edgesets, degree, ts)
      }
      case "url" => {
        val url = map.getOrElse("url", "").toString
        val title = map.getOrElse("title", "").toString
        URLNode(id, url, title, edgesets, degree, ts)
      }
      case "src" => {
        SourceNode(id, edgesets)
      }
      case "rule" => {
        val rule = map.getOrElse("rule", "").toString
        RuleNode(id, rule, edgesets, degree, ts)
      }
      case "usr" => {
        val username = map.getOrElse("username", "").toString
        val name = map.getOrElse("name", "").toString
        val email = map.getOrElse("email", "").toString
        val pwdhash = map.getOrElse("pwdhash", "").toString
        val role = map.getOrElse("role", "").toString
        val session = map.getOrElse("session", "").toString
        val sessionTs = map.getOrElse("sessionTs", "").toString.toLong
        val lastSeen = map.getOrElse("lastSeen", "").toString.toLong
        UserNode(id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, edgesets, degree, ts)
      }
      case "usre" => {
        val username = map.getOrElse("username", "").toString
        val email = map.getOrElse("email", "").toString
        UserEmailNode(id, username, email, edgesets, degree, ts)
      }
      case _  => throw WrongVertexType("unkown vtype: " + vtype)
    }
  }

  def rawget(id: String): String = backend.rawget(id)

  /** Adds Vertex to database */
  def put(vertex: Vertex): Vertex = {
    backend.put(vertex.id, vertex.toMap)
    vertex
  }

  /** Updates vertex on database */
  def update(vertex: Vertex): Vertex = {
    backend.update(vertex.id, vertex.toMap)
    vertex
  }

  /** Chech if vertex exists on database */
  def exists(id: String): Boolean = {
    try {
      val v = get(id)
    }
    catch {
      case _ => return false
    }
    true
  }

  /** Removes vertex from database */
  def remove(vertex: Vertex): Vertex = {
    backend.remove(vertex.id)
    var extra = 1
    var done = false
    while (!done){
      val extraId = ID.extraId(vertex.id, extra)
      if (exists(extraId)) {
        backend.remove(extraId)
        extra += 1
      }
      else {
        done = true
      }
    }
    vertex
  }

  def relExistsOnVertex(vertex: Vertex, edge: Edge): Boolean = {
    val edgeSetId = ID.edgeSetId(vertex.id, edge.id)

    if (!vertex.edgesets.contains(edgeSetId))
      return false

    val edgeSet = getEdgeSet(edgeSetId)
    if (edgeSet.edges.exists(x => Vertex.cleanId(x) == edge.id))
      return true
    // if extra < 0, no extra vertices exist
    if (edgeSet.extra < 0)
      return false

    // else let's start from extra = 1
    var extra = 1
    while (true) {
      val testVertex = try {
        getExtraEdges(ID.extraId(edgeSet.id, extra))
      }
      catch {
        case _ => null
      }
      if (testVertex == null)
        return false
      if (testVertex.edges.exists(x => Vertex.cleanId(x) == edge.id))
        return true

      extra += 1
    }

    false
  }

  def relExists(edge: Edge): Boolean = {
    val vertex = get(edge.participantIds(0))
    return relExistsOnVertex(vertex, edge)
  }

  def clearEdgeSet(vertex: Vertex, edgeSetId: String) = {
    remove(EdgeSet(edgeSetId))

    var extra = 1
    var done = false
    while (!done) {
      val extraEdges = getExtraEdgesOrNull(ID.extraId(edgeSetId, extra))
      if (extraEdges == null) {
        done = true
      }
      else {
        remove(extraEdges)
        extra += 1
      }
    }
  }

  def addrel(edgeType: String, participants: Array[String]): Boolean = {
    val edge = new Edge(edgeType, participants)

    // bail out if relationship already exists
    if (relExists(edge))
      return false

    // create EdgeType vertex if it does not exist
    if (!exists(edgeType)) {
      val etype = new EdgeType(edgeType)
      put(etype)
    }

    // increment instances counter on EdgeType
    val etype = getEdgeType(edgeType)
    update(etype.setInstances(etype.instances + 1))

    // create edge vertex
    put(edge)

    for (id <- participants) {
      var vertex = get(id)

      val edgeSetId = ID.edgeSetId(vertex.id, edge.id)

      // create edgeset and edgeset reference on vertex if it doesn't exit already
      if (!vertex.edgesets.contains(edgeSetId)) {
        clearEdgeSet(vertex, edgeSetId)
        put(EdgeSet(edgeSetId))
        update(vertex.setEdgeSets(vertex.edgesets + edgeSetId))
        vertex = get(id)
      }

      // increment participant's degree
      if (!ID.isInSystemSpace(edgeType)) {
        update(vertex.setDegree(vertex.degree + 1))
      }

      
      // increment edgeset's size
      var edgeSet = getEdgeSet(edgeSetId)
      put(edgeSet.setSize(edgeSet.size + 1))

      // add edge to appropriate edgeset or extraedges vertex
      edgeSet = getEdgeSet(edgeSetId)      
      val origExtra = if (edgeSet.extra >= 0) edgeSet.extra else 0
      var extra = origExtra
      var done = false
      while (!done) {
        if (extra == 0) {
          if (edgeSet.edges.size < maxEdges) {
            done = true;
            update(edgeSet.setEdges(edgeSet.edges + edge.id))
          }
          else {
            extra += 1
          }
        }
        else {
          val extraId = ID.extraId(edgeSetId, extra)
          val extraEdges = getExtraEdgesOrNull(extraId)
          if (extraEdges == null) {
            done = true
            put(ExtraEdges(extraId, Set[String](edge.id)))
            update(edgeSet.setExtra(extra))
          }
          else if (extraEdges.edges.size < maxEdges) {
            done = true;
            update(extraEdges.setEdges(extraEdges.edges + edge.id))
            if (origExtra != extra) {
              update(edgeSet.setExtra(extra))
            }
          }
          else {
            extra += 1
          }
        }
      }
    }

    true
  }

  def addrel(edgeId: String): Boolean = addrel(Edge.edgeType(edgeId), Edge.participantIds(edgeId).toArray)

  def isEdgeSetEmpty(edgeSetId: String, vertex: Vertex): Boolean = {
    val edgeSet = getEdgeSet(edgeSetId)

    // edgeset never had extra edges if edges == -1
    if (edgeSet.extra < 0) {
      return (edgeSet.edges.size == 0)
    }
    else {
      var extra = 0
      if (edgeSet.edges.size > 0) {
        return false
      }
      while (true) {
        extra += 1
        val extraEdges = getExtraEdgesOrNull(ID.extraId(edgeSetId, extra))
        if (extraEdges == null) {
          return true
        }
        else if (extraEdges.edges.size > 0) {
          return false
        }
      }
    }

    // this point should never be reached
    true
  }

  def delrel(edgeType: String, participants: Array[String]): Boolean = {
    val edge = new Edge(edgeType, participants)

    if (relExists(edge)) {
      // decrement instances counter on EdgeType
      val etype = getEdgeType(edgeType)
      update(etype.setInstances(etype.instances - 1))

      for (id <- participants) {
        var vertex = get(id)

        // decrement participant's degree
        update(vertex.setDegree(vertex.degree - 1))

        val edgeSetId = ID.edgeSetId(id, edge.id)
      
        var edgeSet = getEdgeSet(edgeSetId)
      
        // decrement edgeset's size
        put(edgeSet.setSize(edgeSet.size - 1))
        edgeSet = getEdgeSet(edgeSetId)

        // edgeset never had extra edges if edges == -1
        if (edgeSet.extra < 0) {
          update(edgeSet.setEdges(edgeSet.edges - edge.id))
        }
        else {
          var done = false
          var extra = 0
          var extendedId = edgeSet.edges.find(x => Vertex.cleanId(x) == edge.id)
          extendedId match {
            case None =>
            case Some(x) => {
              done = true
              update(edgeSet.setEdges(edgeSet.edges - x))
            }
          }
          while (!done) {
            extra += 1
            val extraEdges = getExtraEdges(ID.extraId(edgeSetId, extra))
            
            if (extraEdges.id != "") {
              extendedId = extraEdges.edges.find(x => Vertex.cleanId(x) == edge.id)
              extendedId match {
                case None =>
                case Some(x) => {
                  done = true
                  update(extraEdges.setEdges(extraEdges.edges - x))
                }
              }
            }
          }

          // update extra on participant's edgeset if needed
          // the idea is to reuse slots that get released on ExtraEdges associated with EdgeSets
          if (edgeSet.extra != extra)
            update(getEdgeSet(edgeSetId).setExtra(extra))
        }

        // remove from edgesets if empty
        if (isEdgeSetEmpty(edgeSetId, vertex)) {
          vertex = get(id)
          update(vertex.setEdgeSets(vertex.edgesets - edgeSetId))
        }
      }
    }

    // remove actual edge
    if (exists(edge.id))
      remove(edge)

    true
  }
  
  def delrel(edgeId: String): Boolean = delrel(Edge.edgeType(edgeId), Edge.participantIds(edgeId).toArray)

  def neighbors(nodeId: String): Set[String] = {
    try {
      val nset = MSet[String]()
    
      // add root node
      nset += (nodeId)

      // add nodes connected to root
      val node = get(nodeId)
      for (edgeSetId <- node.edgesets) {
        val edgeSet = getEdgeSet(edgeSetId)
        for (edgeId <- edgeSet.edges) {
          if (!ID.isInSystemSpace(Edge.edgeType(edgeId))) {
            for (eid <- Edge.participantIds(edgeId)) {
              val id = Vertex.cleanId(eid)
              if (id != nodeId) {
                nset += id
              }
            }
          }
        }    
      }

      nset.toSet
    }
    catch {
      case e => Set[String]()
    }
  }

  def edgeInNeighborhood(edgeId: String, nhood: Set[String]): Boolean = {
    val pids = Edge.participantIds(edgeId)
    for (pid <- pids) {
      if (!nhood.contains(pid)) {
        return false
      }
    }

    true
  }

  def neighborEdges(nodeId: String): Set[String] = {
    try {
      val eset = MSet[String]()

      // add edges connected to root
      val node = get(nodeId)
      for (edgeSetId <- node.edgesets) {
        val edgeSet = getEdgeSet(edgeSetId)
        for (edgeId <- edgeSet.edges) {
          eset += Edge.cleanId(edgeId)
        }      
      }

      eset.toSet
    }
    catch {
      case e => Set[String]()
    }
  }

  def neighborEdges(nodeId: String, nhood: Set[String]): Set[String] = {
    try {
      val eset = MSet[String]()

      // add edges connected to root
      val node = get(nodeId)
      for (edgeSetId <- node.edgesets) {
        val edgeSet = getEdgeSet(edgeSetId)
        for (edgeId <- edgeSet.edges) {
          val eid = Edge.cleanId(edgeId)
          if ((!ID.isInSystemSpace(Edge.edgeType(eid))) && (edgeInNeighborhood(eid, nhood))) {
            eset += eid
          }
        }      
      }

      eset.toSet
    }
    catch {
      case e => Set[String]()
    }
  }

  def nodesFromEdgeSet(edgeSet: Set[String]): Set[String] = {
    var nodeList: List[String] = Nil
    for (edge <- edgeSet) {
      val pids = Edge.participantIds(edge)
      for (pid <- pids) {
        nodeList = Vertex.cleanId(pid) :: nodeList
      }
    }

    nodeList.toSet
  }

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

  def getEdges(vertexId: String, pos: Int, rel: String) = new Edges(vertexId, pos, rel, this)
}
