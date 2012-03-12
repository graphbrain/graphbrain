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
    val edges = VertexStore.str2iter(map.getOrElse("edges", "").toString).toSet
    val vtype = map.getOrElse("vtype", "")
    val extra = map.getOrElse("extra", "-1").toString.toInt
    vtype match {
      case "edg" => {
        val etype = map.getOrElse("etype", "").toString
        Edge(id, etype, edges, extra)
      }
      case "ext" => {
        ExtraEdges(id, edges, extra)
      }
      case "edgt" => {
        val label = map.getOrElse("label", "").toString
        val roles = VertexStore.str2iter(map.getOrElse("roles", "").toString).toList
        val rolen = map.getOrElse("rolen", "").toString
        EdgeType(id, label, roles, rolen, edges, extra)
      }
      case "txt" => {
        val text = map.getOrElse("text", "").toString
        TextNode(id, text, edges, extra)
      }
      case "url" => {
        val url = map.getOrElse("url", "").toString
        URLNode(id, url, edges, extra)
      }
      case "src" => {
        SourceNode(id, edges, extra)
      }
      case "img" => {
        val url = map.getOrElse("url", "").toString
        ImageNode(id, url, edges, extra)
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
      val extraId = VertexStore.extraId(vertex.id, extra)
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

  /** Adds relationship to database
    * 
    * An edge is creted and participant nodes are updated with a reference to that edge
    * in order to represent a new relationship on the database.
    * maxEdges limit is respected. If the number of edges stored in a participant reaches
    * this value, ExtraEdges vertices are created and updaed to store the edges.
    */
  def addrel(edgeType: String, participants: Array[String]) = {
    val edge = new Edge(edgeType, participants)

    for (id <- participants) {
      val vertex = get(id)
      val origExtra = if (vertex.extra >= 0) vertex.extra else 0
      var extra = origExtra
      var done = false
      while (!done) {
        if (extra == 0) {
          if (vertex.edges.size < maxEdges) {
            done = true;
            update(vertex.setEdges(vertex.edges + edge.id).setExtra(extra))
          }
          else {
            extra += 1
          }
        }
        else {
          val extraId = VertexStore.extraId(id, extra)
          val extraEdges = getOrNull(extraId)
          if (extraEdges == null) {
            done = true
            put(ExtraEdges(extraId, Set[String](edge.id)))
            update(vertex.setExtra(extra))
          }
          else if (extraEdges.edges.size < maxEdges) {
            done = true;
            update(extraEdges.setEdges(extraEdges.edges + edge.id))
            if (origExtra != extra) {
              update(vertex.setExtra(extra))
            }
          }
          else {
            extra += 1
          }
        }
      }
    }

    put(edge)
  }

  /** Deletes relationship from database
    * 
    * The edge defining the relationship is removed and participant nodes are updated 
    * to drop the reference to that edge.
    */
  def delrel(edgeType: String, participants: Array[String]) = {
    val edge = new Edge(edgeType, participants)
    remove(edge)

    for (nodeId <- participants) {
      val node = get(nodeId)
      // vertex never had extra edges if edges == -1
      if (node.extra < 0) {
        update(node.setEdges(node.edges - edge.id))
      }
      else {
        var done = false
        var extra = 0
        if (node.edges.contains(edge.id)) {
          done = true
          update(node.setEdges(node.edges - edge.id))
        }
        while (!done) {
          extra += 1
          val extraEdges = get(VertexStore.extraId(nodeId, extra))
          // this should now happen
          if (extraEdges.id == "") {

          }
          else if (extraEdges.edges.contains(edge.id)) {
            done = true
            update(extraEdges.setEdges(extraEdges.edges - edge.id))
          }
        }

        // update extra on participant if needed
        // this idea is to reuse slots that get released on ExtraEdges associated with vertices
        if (node.extra != extra) update(get(nodeId).setExtra(extra))
      }
    }

    edge
  }

  
  def neighbors(nodeId: String, maxDepth: Int = 2): Set[(String, String)] = {
    val nset = MSet[(String, String)]()

    var queue = (nodeId, 0, "") :: Nil
    while (!queue.isEmpty) {
      val curId = queue.head._1
      val depth = queue.head._2
      val parent = queue.head._3
      queue = queue.tail

      if (!nset.exists(n => n._1 == curId)) {
        nset += ((curId, parent))
        val node = get(curId)

        if (depth < maxDepth)
          for (edgeId <- node.edges)
            queue = queue ::: (for (pid <- Edge.participantIds(edgeId)) yield (pid, depth + 1, curId)).toList
      }
    }

    nset.toSet
  }

  /** Gets all Edges that are internal to a neighborhood 
    * 
    * An Edge is considered inernal if all it's participating Nodes are containes in the
    * neighborhood.
    */
  def neighborEdges(nhood: Set[(String, String)]): Set[String] = {
    val nhoodIds = for (n <- nhood) yield n._1
    val eset = MSet[String]()
    for (n <- nhood) {
      val node = get(n._1)
      for (edgeId <- node.edges)
        if (Edge.participantIds(edgeId).forall(nhoodIds.contains(_)))
          eset += edgeId
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

  private def extraId(id: String, pos: Int) =  id + "/" + pos
} 