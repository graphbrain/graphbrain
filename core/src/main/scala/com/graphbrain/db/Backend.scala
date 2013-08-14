package com.graphbrain.db

import VertexType.VertexType

trait Backend {
	def close(): Unit
	def get(id: String, vtype: VertexType): Vertex
	def put(vertex: Vertex): Vertex
	def update(vertex: Vertex): Vertex
	def remove(vertex: Vertex)
	def associateEmailToUsername(email: String, username: String)
	def usernameByEmail(email: String): String
	def listByType(vtype: VertexType): List[Vertex]
	def edges(center: Vertex): Set[Edge]
  def edges(pattern: Edge): Set[Edge]
  def addLinkToGlobal(globalId: String, userId: String)
  def removeLinkToGlobal(globalId: String, userId: String)
  def alts(globalId: String): Set[String]
}