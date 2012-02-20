package com.graphbrain.hgdb

import scala.collection.mutable.{Set => MSet}

/** Vertex store.
  *
  * Implements and hypergraph database on top of a key/Map store. 
  */
class VertexStore(storeName: String) extends VertexStoreInterface{
  val backend: Backend = new RiakBackend(storeName)

  /** Gets Vertex by it's id */
  override def get(id: String): Vertex = {
    val map = backend.get(id)
    map("vtype") match {
      case "vertex" => Vertex(id)
      case "node" => {
        val edges = map.getOrElse("edges", "").toString
        Node(id, edges)
      }
      case "textnode" => {
        val edges = map.getOrElse("edges", "").toString
        val text = map.getOrElse("text", "").toString
        TextNode(id, text, edges)
      }
      case "imagenode" => {
        val edges = map.getOrElse("edges", "").toString
        val url = map.getOrElse("url", "").toString
        ImageNode(id, url, edges)
      }
      case "edge" => {
        val etype = map.getOrElse("etype", "").toString
        Edge(id, etype)
      }
      case "edgeType" => {
        val label = map.getOrElse("label", "").toString
        EdgeType(id, label)
      }
      // TODO: throw exception
      case _  => Vertex(id)
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

  /** Removes vertex from database */
  override def remove(vertex: Vertex): Vertex = {
    backend.remove(vertex.id)
    vertex
  }

  /** Adds relationship to database
    * 
    * An edge is creted and participant nodes are updated with a reference to that edge
    * in order to represent a new relationship on the database.
    */
  def addrel(edgeType: String, participants: Array[String]) = {
    val edge = Edge(edgeType, participants)
    put(edge)

    for (nodeId <- participants) update(getNode(nodeId).addEdge(edge.id))

    edge
  }

  /** Deletes relationship from database
    * 
    * The edge defining the relationship is removed and participant nodes are updated 
    * to drop the reference to that edge.
    */
  def delrel(edgeType: String, participants: Array[String]) = {
    val edge = Edge(edgeType, participants)
    remove(edge)

    for (nodeId <- participants) update(getNode(nodeId).delEdge(edge.id))

    edge
  }

  /** Gets Node by it's id */
  def getNode(nodeId: String): Node = get(nodeId) match {
    case n: Node => n
    // TODO: throw exception if it's not a Node
    case _ => Node("", "")
  }

  /** Gets neighbors of a Node specified by id
    * 
    * maxDepth is the maximum distance from the original node that will be considered (default is 2)
    */
  def neighbors(nodeId: String, maxDepth: Int = 2): Set[String] = {
    val nset = MSet[String]()

    var queue = (nodeId, 0) :: Nil
    while (!queue.isEmpty) {
      val curId = queue.head._1
      val depth = queue.head._2
      queue = queue.tail

      if (!nset.contains(curId)) {
        nset += curId
        val node = getNode(curId)

        if (depth < maxDepth)
          for (edgeId <- node.edges)
            queue = queue ::: (for (pid <- Edge.participantIds(edgeId)) yield (pid, depth + 1)).toList
      }
    }

    nset.toSet
  }

  /** Gets all Edges that are internal to a neighborhood 
    * 
    * An Edge is considered inernal if all it's participating Nodes are containes in the
    * neighborhood.
    */
  def neighborEdges(nhood: Set[String]): Set[String] = {
    val eset = MSet[String]()
    for (nodeId <- nhood) {
      val node = getNode(nodeId)
      for (edgeId <- node.edges)
        if (Edge.participantIds(edgeId).forall(nhood.contains(_)))
          eset += edgeId
    }
    eset.toSet
  }
}

object VertexStore {
  def apply(storeName: String) = new VertexStore(storeName)
} 