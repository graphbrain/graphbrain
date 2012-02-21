package com.graphbrain.hgdb

class EdgeType(id: String, val label: String, edges: Set[String]) extends Node (id, edges) {
  override val vtype = "edgeType"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("label" -> label))
}

object EdgeType {
  def apply(id: String, label: String, edges: Set[String] = Set[String]()) = new EdgeType(id, label, edges)
  def apply(id: String, label: String, edgesStr: String) = new EdgeType(id, label, Node.str2set(edgesStr))
}