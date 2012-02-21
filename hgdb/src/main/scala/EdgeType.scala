package com.graphbrain.hgdb

class EdgeType(id: String, val label: String, val roles: List[String],
  val rolen: String, edges: Set[String]) extends Node (id, edges) {
  
  override val vtype = "edgeType"

  override def toMap: Map[String, Any] = super.toMap ++
    Map(("label" -> label), ("roles" -> Node.iter2str(roles)), ("rolen" -> rolen))
}

object EdgeType {
  def apply(id: String, label: String, roles: List[String], rolen: String = "",
    edges: Set[String] = Set[String]()) = new EdgeType(id, label, roles, rolen, edges)
  def apply(id: String, label: String, roles: String, rolen: String,
    edgesStr: String) = new EdgeType(id, label, Node.str2iter(roles).toList, rolen, Node.str2iter(edgesStr).toSet)
}