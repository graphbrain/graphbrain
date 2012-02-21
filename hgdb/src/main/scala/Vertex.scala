package com.graphbrain.hgdb

class Vertex(val id: String, val edges: Set[String]) {
  val vtype = "vertex"

  def toMap: Map[String, Any] = Map(("vtype" -> vtype), ("edges" -> Vertex.iter2str(edges)))

  def addEdge(edgeId: String): Node = Node(id, edges + edgeId)

  def delEdge(edgeId: String): Node = Node(id, edges - edgeId)

  override def toString: String = vtype + " [" + id + "]; edges: " + Vertex.iter2str(edges)
}

object Vertex {
  def apply(id: String, edges: Set[String] = Set[String]()) = new Vertex(id, edges)
  def apply(id: String, edgesStr: String) = new Vertex(id, str2iter(edgesStr).toSet)

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