package com.graphbrain

object HGDB extends App{ 
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

  def put(vertex: Vertex) = {
    Store.put(vertex.toMap)
    vertex
  }

  def update(vertex: Vertex) = {
    Store.update(vertex._id, vertex.toMap)
    vertex
  }

  override def main(args: Array[String]) = {
    println(update(Edge("e44", List[String](), "gis3")))
    println(get("e44"))
    //println(Vertex("wikipedia/alan_ball_(screenwriter)"))
  }
}