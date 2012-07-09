package com.graphbrain.hgdb

import scala.collection.mutable.{Set => MSet}


/** Vertex store.
  *
  * Implements and hypergraph database on top of a key/Map store. 
  */
class VertexStore(storeName: String, val maxEdges: Int = 1000, ip: String="127.0.0.1", port: Int=8098) extends VertexStoreInterface {
  val backend: Backend = new RiakBackend(storeName, ip, port)

  /** Gets Vertex by it's id */
  override def get(id: String): Vertex = {
    val map = backend.get(id)
    val edgesets = VertexStore.str2iter(map.getOrElse("edgesets", "").toString).toSet
    val vtype = map.getOrElse("vtype", "")
    val degree = map.getOrElse("extra", "0").toString.toInt
    vtype match {
      case "edg" => {
        val etype = map.getOrElse("etype", "").toString
        Edge(id, etype, edgesets, degree)
      }
      case "edgs" => {
        val edges = VertexStore.str2iter(map.getOrElse("edges", "").toString).toSet
        val extra = map.getOrElse("extra", "-1").toString.toInt
        val size = map.getOrElse("size", "0").toString.toInt
        EdgeSet(id, edges, extra, size) 
      }
      case "ext" => {
        val edges = VertexStore.str2iter(map.getOrElse("edges", "").toString).toSet
        ExtraEdges(id, edges) 
      }
      case "edgt" => {
        val label = map.getOrElse("label", "").toString
        EdgeType(id, label, edgesets, degree)
      }
      case "txt" => {
        val text = map.getOrElse("text", "").toString
        TextNode(id, text, edgesets, degree)
      }
      case "url" => {
        val url = map.getOrElse("url", "").toString
        val title = map.getOrElse("title", "").toString
        URLNode(id, url, title, edgesets, degree)
      }
      case "src" => {
        SourceNode(id, edgesets)
      }
      case "img" => {
        val url = map.getOrElse("url", "").toString
        ImageNode(id, url, edgesets, degree)
      }
      case "vid" => {
        val url = map.getOrElse("url", "").toString
        VideoNode(id, url, edgesets, degree)
      }
      case "svg" => {
        val svg = map.getOrElse("svg", "").toString
        SVGNode(id, svg, edgesets, degree)
      }

      case "rule" => {
        val rule = map.getOrElse("rule", "").toString
        RuleNode(id, rule, edgesets, degree)
      }
      case "usr" => {
        val username = map.getOrElse("username", "").toString
        val name = map.getOrElse("name", "").toString
        val email = map.getOrElse("email", "").toString
        val pwdhash = map.getOrElse("pwdhash", "").toString
        val role = map.getOrElse("role", "").toString
        val session = map.getOrElse("session", "").toString
        val creationTs = map.getOrElse("creationTs", "").toString.toLong
        val sessionTs = map.getOrElse("sessionTs", "").toString.toLong
        val lastSeen = map.getOrElse("lastSeen", "").toString.toLong
        UserNode(id, username, name, email, pwdhash, role, session, creationTs, sessionTs, lastSeen, edgesets, degree)
      }
      case "usre" => {
        val username = map.getOrElse("username", "").toString
        val email = map.getOrElse("email", "").toString
        UserEmailNode(id, username, email, edgesets, degree)
      }
      case _  => throw WrongVertexType("unkown vtype: " + vtype)
    }
  }

  /** Adds Vertex to database */
  override def put(vertex: Vertex): Vertex = {
    backend.put(vertex.id, vertex.toMap)
    vertex
  }

  /** Updates vertex on database */
  override def update(vertex: Vertex): Vertex = {
    backend.update(vertex.id, vertex.toMap)
    vertex
  }

  /** Chech if vertex exists on database */
  def exists(id: String): Boolean = {
    try {
      get(id)
    }
    catch {
      case _ => return false
    }
    true
  }

  /** Removes vertex from database */
  override def remove(vertex: Vertex): Vertex = {
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
    if (edgeSet.edges.contains(edge.id))
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
      if (testVertex.edges.contains(edge.id))
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

    if (relExists(edge))
      return false

    put(edge)

    for (id <- participants) {
      val vertex = get(id)
      val edgeSetId = ID.edgeSetId(vertex.id, edge.id)

      // create edgeset and edgeset reference on vertex if it doesn't exit already
      if (!vertex.edgesets.contains(edgeSetId)) {
        clearEdgeSet(vertex, edgeSetId)
        put(EdgeSet(edgeSetId))
        update(vertex.setEdgeSets(vertex.edgesets + edgeSetId))
      }

      // add edge to appropriate edgeset or extraedges vertex
      val edgeSet = getEdgeSet(edgeSetId)
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

  def delrel(edgeType: String, participants: Array[String]): Unit = {
    val edge = new Edge(edgeType, participants)

    for (nodeId <- participants) {
      val node = get(nodeId)
      val edgeSetId = ID.edgeSetId(node.id, edge.id)
      val edgeSet = getEdgeSet(edgeSetId)

      // edgeset never had extra edges if edges == -1
      if (edgeSet.extra < 0) {
        update(edgeSet.setEdges(edgeSet.edges - edge.id))
      }
      else {
        var done = false
        var extra = 0
        if (edgeSet.edges.contains(edge.id)) {
          done = true
          update(edgeSet.setEdges(edgeSet.edges - edge.id))
        }
        while (!done) {
          extra += 1
          val extraEdges = getExtraEdges(ID.extraId(edgeSetId, extra))
          // this should not happen
          if (extraEdges.id == "") {

          }
          else if (extraEdges.edges.contains(edge.id)) {
            done = true
            update(extraEdges.setEdges(extraEdges.edges - edge.id))
          }
        }

        // update extra on participant's edgeset if needed
        // this idea is to reuse slots that get released on ExtraEdges associated with EdgeSets
        if (edgeSet.extra != extra) update(getEdgeSet(edgeSetId).setExtra(extra))
      }

      // remove from edgesets if empty
      if (isEdgeSetEmpty(edgeSetId, node)) {
        update(node.setEdgeSets(node.edgesets - edgeSetId))
      }
    }
  }
  
  def neighbors(nodeId: String): Set[(String, String)] = {
    val nset = MSet[(String, String)]()
    
    // add root node
    nset += ((nodeId, ""))

    // add nodes connected to root
    val node = get(nodeId)
    for (edgeSetId <- node.edgesets) {
      val edgeSet = getEdgeSet(edgeSetId)
      for (edgeId <- edgeSet.edges) {
        for (pid <- Edge.participantIds(edgeId)) {
          if (pid != nodeId) {
            nset += ((pid, nodeId))
          }
        }
      }      
    }

    nset.toSet
  }

  def neighborEdges(nodeId: String): Set[String] = {
    val eset = MSet[String]()

    // add edges connected to root
    val node = get(nodeId)
    for (edgeSetId <- node.edgesets) {
      val edgeSet = getEdgeSet(edgeSetId)
      for (edgeId <- edgeSet.edges) {
        eset += edgeId
      }      
    }

    eset.toSet
  }
}

object VertexStore {
  def apply(storeName: String) = new VertexStore(storeName)

  private def str2iter(str: String) = {
    (for (str <- str.split(',') if str != "")
      yield str.replace("$2", ",").replace("$1", "$")).toIterable
  }
}