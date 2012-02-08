package com.graphbrain

object HGDB { 
  def get(_id: String) = {
    val map = Store.get(_id)
    val rid = map.getOrElse("_id", "").toString
    val links: List[String] = map.getOrElse("links", List[String]()).asInstanceOf[List[String]]
    map("vtype") match {
      case "vertex" => Vertex(rid, links)
      case "node" => Node(rid, links)
      case "edge" => {
        val etype = map.getOrElse("etype", "").toString
        Edge(rid, links, etype)
      }
      case _  => Vertex(rid, links)
    }
  }

  def store(vertex: Vertex) = {
    Store.put(vertex.toMap)
    vertex
  }

  def main(args: Array[String]) = {
    println(store(Edge("e14", List[String](), "is")))
    println(get("e14"))
    //println(Vertex("wikipedia/alan_ball_(screenwriter)"))
  }
}