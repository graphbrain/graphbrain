package com.graphbrain.hgdb

class EdgeType(id: String, val label: String, val roles: List[String],
  val rolen: String, edges: Set[String]) extends Vertex (id, edges) {
  
  override val vtype = "edgeType"

  override def toMap: Map[String, Any] = super.toMap ++
    Map(("label" -> label), ("roles" -> Vertex.iter2str(roles)), ("rolen" -> rolen))
}

object EdgeType {
  def apply(id: String, label: String, roles: List[String], rolen: String = "",
    edges: Set[String] = Set[String]()) = new EdgeType(id, label, roles, rolen, edges)
  def apply(id: String, label: String, roles: String, rolen: String,
    edgesStr: String) = new EdgeType(id, label, Vertex.str2iter(roles).toList, rolen, Vertex.str2iter(edgesStr).toSet)
}