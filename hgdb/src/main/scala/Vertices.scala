package com.graphbrain.hgdb


abstract class Vertex {
  val id: String

  def extendedId: String = id

  def clone(newid: String): Vertex

  override def toString: String = id
}

object Vertex {
  def cleanId(id: String) = id.toLowerCase
} 


case class EdgeType(id: String="", label: String="", instances: Long = 0) extends Vertex {

  override def clone(newid: String) = EdgeType(newid, label)

  def setInstances(newInst: Long) = copy(instances=newInst)
}


case class TextNode(namespace: String="", text: String="") extends Vertex {
  
  override val id = namespace + "/" + ID.sanitize(text).toLowerCase

  override def extendedId: String = namespace + "/" + ID.sanitize(text)

  override def clone(newid: String) = TextNode(ID.namespace(newid), text)

  override def toString: String = text
}


//To store the rule body
case class RuleNode(id: String="", rule: String="") extends Vertex {
  
  override def clone(newid: String) = RuleNode(newid, rule)

  override def toString: String = rule
}


case class URLNode(id: String="", url: String="", title: String="") extends Vertex {

  override def clone(newid: String) = URLNode(newid, url)

  def setTitle(newTitle: String) = copy(title=newTitle)
}


case class SourceNode(id: String="") extends Vertex {

  override def clone(newid: String) = SourceNode(newid)
}


case class UserNode(id: String="", username: String="", name: String="", email: String="",
  pwdhash: String="", role: String="", session: String="", sessionTs: Long= -1,
  lastSeen: Long= -1) extends Vertex {

  override def clone(newid: String) = this

  override def toString: String = name
}