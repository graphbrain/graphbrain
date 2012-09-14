package com.graphbrain.hgdb


abstract class Vertex {
  val id: String
  val degree: Long

  def extendedId: String = id

  def clone(newid: String): Vertex

  def setDegree(newDegree: Long): Vertex

  override def toString: String = id
}

object Vertex {
  def cleanId(id: String) = id.toLowerCase
} 


case class EdgeType(id: String="", label: String="", instances: Long = 0, degree: Long=0) extends Vertex {

  override def clone(newid: String) = EdgeType(newid, label)

  def setDegree(newDegree: Long) = copy(degree=newDegree)
  def setInstances(newInst: Long) = copy(instances=newInst)
}


case class TextNode(namespace: String="", text: String="", degree: Long=0) extends Vertex {
  
  override val id = namespace + "/" + ID.sanitize(text).toLowerCase

  override def extendedId: String = namespace + "/" + ID.sanitize(text)

  override def clone(newid: String) = TextNode(ID.namespace(newid), text)

  def setDegree(newDegree: Long) = copy(degree=newDegree)

  override def toString: String = text
}


//To store the rule body
case class RuleNode(id: String="", rule: String="", degree: Long=0) extends Vertex {
  
  override def clone(newid: String) = RuleNode(newid, rule)

  def setDegree(newDegree: Long) = copy(degree=newDegree)

  override def toString: String = rule
}


case class URLNode(id: String="", url: String="", title: String="", degree: Long=0) extends Vertex {

  override def clone(newid: String) = URLNode(newid, url)

  def setDegree(newDegree: Long) = copy(degree=newDegree)
  def setTitle(newTitle: String) = copy(title=newTitle)
}


case class SourceNode(id: String="", degree: Long=0) extends Vertex {

  override def clone(newid: String) = SourceNode(newid)

  def setDegree(newDegree: Long) = copy(degree=newDegree)
}


case class UserNode(id: String="", username: String="", name: String="", email: String="",
  pwdhash: String="", role: String="", session: String="", sessionTs: Long= -1,
  lastSeen: Long= -1, degree: Long=0) extends Vertex {

  override def clone(newid: String) = this

  override def toString: String = name

  def setDegree(newDegree: Long) = copy(degree=newDegree)
}