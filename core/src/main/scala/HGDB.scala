package com.graphbrain

class HGDB(storeName: String) {
  val store = new Store(storeName)

  def get(_id: String) = {
    val map = store.get(_id)
    val rid = map.getOrElse("_id", "").toString
    map("vtype") match {
      case "vertex" => Vertex(rid)
      case "node" => {
        val edges: Set[String] = map.getOrElse("edges", List[String]()).asInstanceOf[List[String]].toSet
        Node(rid, edges)
      }
      case "edge" => {
        val etype = map.getOrElse("etype", "").toString
        Edge(rid, etype)
      }
      case "edgeType" => EdgeType(rid)
      case _  => Vertex(rid)
    }
  }

  def put(vertex: Vertex) = {
    store.put(vertex.toMap)
    vertex
  }

  def update(vertex: Vertex) = {
    store.update(vertex._id, vertex.toMap)
    vertex
  }

  def remove(vertex: Vertex) = store.remove(vertex._id)

  def addEdge(edgeType: String, participants: Array[Node]) = {
    val eid = edgeType + (for (node <- participants) yield (" " + node._id))
    val edge = Edge(eid, edgeType)
    put(edge)

    for (node <- participants) node.addEdge(edge)

    edge
  }
}

object HGDB {
  def apply(storeName: String) = new HGDB(storeName)
}