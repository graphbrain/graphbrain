package com.graphbrain.hgdb

class Vertex(val id: String, val edges: Set[String]) {
  def toMap: Map[String, Any] = Map(("vtype" -> "vertex")) ++ toMapBase
  def toMapBase: Map[String, Any] = Map(("edges" -> Vertex.iter2str(edges)))
  
  def addEdge(edgeId: String): Vertex = new Vertex(id, edges + edgeId)
  def delEdge(edgeId: String): Vertex = new Vertex(id, edges - edgeId)

  override def toString: String = "[" + id + "]; edges: " + Vertex.iter2str(edges)
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