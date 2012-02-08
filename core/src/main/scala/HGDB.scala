package com.graphbrain

object HGDB { 
  def get(_id: String) = {
    val map = Store.get(_id)
    val links: List[String] = map.getOrElse("links", List[String]()).asInstanceOf[List[String]]
    map("vtype") match {
      case "vertex" => Vertex(map("_id").toString, links)
      case "node" => Node(map("_id").toString, links)
      case "edge" => Edge(map("_id").toString, links)
      case _  => Vertex(map("_id").toString, links)
    }
  }

  def store(vertex: Vertex) = {
    Store.put(vertex.toMap)
    vertex
  }

  def main(args: Array[String]) = {
    println(store(Edge("e11")))
    println(get("e11"))
    //println(Vertex("wikipedia/alan_ball_(screenwriter)"))
  }
}