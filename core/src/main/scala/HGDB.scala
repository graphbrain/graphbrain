package com.graphbrain

object HGDB { 
  def get(_id: String) = {
    val map = Store.get(_id)
    map("vtype") match {
      case "vertex" => new Vertex(map("_id").toString)
      case _  => new Vertex(map("_id").toString)
    }
  }

  def store(vertex: Vertex) = {
    Store.put(vertex.toMap)
    vertex
  }

  def main(args: Array[String]) = {
    println(store(Vertex("x1")))
    println(get("x1"))
    //println(Vertex("wikipedia/alan_ball_(screenwriter)"))
  }
}