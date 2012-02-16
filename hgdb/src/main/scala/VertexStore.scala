package com.graphbrain.hgdb

import scala.collection.mutable.{Set => MSet}

/** Vertex store.
  *
  * Implements and hypergraph database on top of a key/Map store. 
  */
class VertexStore(storeName: String) {
  val store = new MapStore(storeName)

  /** Gets Vertex by it's id */
  def get(id: String) = {
    val map = store.get(id)
    map("vtype") match {
      case "vertex" => Vertex(id)
      case "node" => {
        val edges = map.getOrElse("edges", "").toString
        Node(id, edges)
      }
      case "edge" => {
        val etype = map.getOrElse("etype", "").toString
        Edge(id, etype)
      }
      case "edgeType" => {
        val label = map.getOrElse("label", "").toString
        EdgeType(id, label)
      }
      case _  => Vertex(id)
    }
  }

  /** Adds Vertex to database */
  def put(vertex: Vertex) = {
    store.put(vertex.id, vertex.toMap)
    vertex
  }

  /** Updates vertex on database */
  def update(vertex: Vertex) = {
    store.update(vertex.id, vertex.toMap)
    vertex
  }

  /** Removes vertex from database */
  def remove(vertex: Vertex) = store.remove(vertex.id)

  /** Adds relationship to database
    * 
    * An edge is creted and participant nodes are updated with a reference to that edge
    * in order to represent a new relationship on the database.
    */
  def addrel(edgeType: String, participants: Array[String]) = {
    val edge = Edge(edgeType, participants)
    put(edge)

    for (nodeId <- participants) update(getNode(nodeId).addEdge(edge))

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

    for (nodeId <- participants) update(getNode(nodeId).delEdge(edge))

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