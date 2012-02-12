package com.graphbrain

/** Hypergraph Data Base.
  *
  * Implements and hypergraph database on top of a simple key/value store. 
  */
class HGDB(storeName: String) {
  val store = new Store(storeName)

  /** Gets Vertex by it's _id */
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
  def addrel(edgeType: String, participants: Array[Node]) = {
    val edge = Edge(edgeType, participants)
    put(edge)

    for (node <- participants) node.addEdge(edge)

    edge
  }

  /** Deletes relationship from database
    * 
    * The edge defining the relationship is removed and participant nodes are updated 
    * to drop the reference to that edge.
    */
  def delrel(edgeType: String, participants: Array[Node]) = {
    val edge = Edge(edgeType, participants)
    remove(edge)

    for (node <- participants) node.delEdge(edge)

    edge
  }
}

object HGDB {
  def apply(storeName: String) = new HGDB(storeName)
}