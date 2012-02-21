package com.graphbrain.hgdb


class Node(id: String, edges: Set[String]) extends Vertex(id, edges) {
  override val vtype = "node"
}

object Node {
  def apply(id: String, edges: Set[String] = Set[String]()) = new Node(id, edges)
  def apply(id: String, edgesStr: String) = new Node(id, Vertex.str2iter(edgesStr).toSet)
}