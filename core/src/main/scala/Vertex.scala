package com.graphbrain

class Vertex(fields: Map[String, Any]) {
  val _id = fields.getOrElse("_id", "").toString
  val label = fields.getOrElse("label", "").toString
  val targs = fields.getOrElse("targs", "").toString

  override def toString: String = _id + ": " + label + " ... " + targs
}

object Vertex {
  def apply(fields: Map[String, Any]) = new Vertex(fields) 

  def apply(_id: String) = new Vertex(Store.get(_id))

  def main(args: Array[String]) = {
    println(Vertex("test"))
    //println(Vertex("wikipedia/alan_ball_(screenwriter)"))
  }
}