package com.graphbrain.core

trait Backend {
	def close: Boolean
	def get(id: String, type: VertexType): Vertex
	def put(vertex: Vertex): Vertex
	def update(vertex: Vertex): Vertex
	def remove(vertex: Vertex)
	def associateEmailToUsername(email: String, username: String)
	def usernameByEmail(email: String): String
	def listByType(type: VertexType): List[Vertex]
	def edges(center: Vertex): Set[Edge]
}