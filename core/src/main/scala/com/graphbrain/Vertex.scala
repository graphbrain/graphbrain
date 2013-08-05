package com.graphbrain.gbdb


abstract class Vertex {
  val store: VertexStore

  val id: String

  def extendedId: String = id

  def put(): Vertex

  def remove() = store.remove(this)

  def clone(newid: String): Vertex

  def toGlobal: Vertex = this

  def toUser(newUserId: String): Vertex = this

  def removeContext: Vertex = this

  def setContext(newContext: String): Vertex = this

  override def toString: String = id

  def description: String = toString

  def raw: String = ""

  def shouldUpdate: Boolean = !store.exists(id)

  def updateFromEdges(): Vertex = this
}

object Vertex {
  def cleanId(id: String) = id.toLowerCase
}