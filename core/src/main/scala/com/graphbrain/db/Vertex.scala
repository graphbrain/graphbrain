package com.graphbrain.db

//import scala.collection.mutable
import VertexType.VertexType

abstract class Vertex {

  val id: String
  val degree: Int = 0
  val ts: Long = -1

  def vtype: VertexType
  def extraMap: Map[String, String]

  def toMap: Map[String, String] =
    Map("degree" -> "" + degree.toString,
      "ts" -> "" + ts.toString) ++
    extraMap

  def clone(newid: String): Vertex

  def toGlobal: Vertex = this

  def toUser(newUserId: String): Vertex = this

  def removeContext(): Vertex = this

  def setContext(newContext: String): Vertex = this

  override def toString: String = id

  def description: String = toString

  def raw: String = ""

  //def shouldUpdate: Boolean = !store.exists(id)

  def updateFromEdges(): Vertex = this
}

object Vertex {
  def cleanId(id: String) = id.toLowerCase
}