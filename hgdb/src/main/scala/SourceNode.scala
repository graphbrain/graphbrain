package com.graphbrain.hgdb


class SourceNode(id: String, edges: Set[String]) extends Node(id, edges) {
  override val vtype = "sourcenode"

}

object SourceNode {
  def apply(id: String, edges: Set[String] = Set[String]()) = new SourceNode(id, edges)
  def apply(id: String, edgesStr: String) = new SourceNode(id, Vertex.str2iter(edgesStr).toSet)
}