package com.graphbrain.hgdb

class Vertex(val id: String) {
  val vtype = "vertex"

  def toMap: Map[String, Any] = Map(("vtype" -> vtype))

  override def toString: String = vtype + " [" + id + "]"
}

object Vertex {
  def apply(id: String) = new Vertex(id) 
}