package com.graphbrain

object HGDB extends App{ 
  def get(_id: String) = {
    val map = Store.get(_id)
    val rid = map.getOrElse("_id", "").toString
    val links: Set[String] = map.getOrElse("links", List[String]()).asInstanceOf[List[String]].toSet
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

  def put(vertex: Vertex) = {
    Store.put(vertex.toMap)
    vertex
  }

  def update(vertex: Vertex) = {
    Store.update(vertex._id, vertex.toMap)
    vertex
  }

  override def main(args: Array[String]) = {
    println(put(Edge("edgy", Set[String](), "says")))
    println(get("edgy"))
    //println(Vertex("wikipedia/alan_ball_(screenwriter)"))
  }
}