package com.graphbrain.hgdb


class Node(id: String, val edges: Set[String]) extends Vertex(id) {
  override val vtype = "node"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("edges" -> Node.iter2str(edges)))

  def addEdge(edgeId: String): Node = Node(id, edges + edgeId)

  def delEdge(edgeId: String): Node = Node(id, edges - edgeId)

  override def toString: String = super.toString + "; edges: " + Node.iter2str(edges)
}

object Node {
  def apply(id: String, edges: Set[String] = Set[String]()) = new Node(id, edges)
  def apply(id: String, edgesStr: String) = new Node(id, str2iter(edgesStr).toSet)

  def iter2str(iter: Iterable[String]) = {
    if (iter.size == 0)
      ""
    else
      (for (str <- iter)
        yield str.replace("$", "$1").replace(",", "$2")).reduceLeft(_ + "," + _)
  }

  def str2iter(str: String) = {
    (for (str <- str.split(',') if str != "")
      yield str.replace("$2", ",").replace("$1", "$")).toIterable
  }
}