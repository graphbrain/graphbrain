package com.graphbrain.hgdb

class EdgeType(id: String, val label: String, val roles: List[String],
  val rolen: String, edges: Set[String]) extends Vertex (id, edges) {
  
  override def toMap: Map[String, Any] = Map(("vtype" -> "edgeType")) ++ toMapBase
  override def toMapBase: Map[String, Any] = super.toMapBase ++
    Map(("label" -> label), ("roles" -> Vertex.iter2str(roles)), ("rolen" -> rolen))

  override def addEdge(edgeId: String): Vertex = new EdgeType(id, label, roles, rolen, edges + edgeId)
  override def delEdge(edgeId: String): Vertex = new EdgeType(id, label, roles, rolen, edges - edgeId)
}

object EdgeType {
  def apply(id: String, label: String, roles: List[String], rolen: String = "",
    edges: Set[String] = Set[String]()) = new EdgeType(id, label, roles, rolen, edges)
  def apply(id: String, label: String, roles: String, rolen: String,
    edgesStr: String) = new EdgeType(id, label, Vertex.str2iter(roles).toList, rolen, Vertex.str2iter(edgesStr).toSet)
}