package com.graphbrain

class Vertex(val _id: String) {
  val vtype = "vertex"

  def toMap: Map[String, Any] = Map(("_id" -> _id), ("vtype" -> vtype))

  override def toString: String = vtype + " [" + _id + "]"
}

object Vertex {
  def apply(_id: String) = new Vertex(_id) 
}