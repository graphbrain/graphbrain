package com.graphbrain

class Vertex(val _id: String, val links: Set[String]) {
  val vtype = "vertex"

  def toMap = Map(("_id" -> _id), ("vtype" -> vtype), ("links" -> links))

  override def toString: String = vtype + " [" + _id + "]"
}

object Vertex {
  def apply(_id: String, links: Set[String] = Set[String]()) = new Vertex(_id, links) 
}