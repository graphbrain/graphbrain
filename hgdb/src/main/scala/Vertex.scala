package com.graphbrain.hgdb


abstract class Vertex {
  val store: VertexStore

  val id: String

  def extendedId: String = id

  def put(): Vertex

  def remove() = store.remove(this)

  def clone(newid: String): Vertex

  override def toString: String = id
}

object Vertex {
  def cleanId(id: String) = id.toLowerCase
}