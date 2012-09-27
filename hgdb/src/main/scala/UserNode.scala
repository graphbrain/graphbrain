package com.graphbrain.hgdb


case class UserNode(id: String="", username: String="", name: String="", email: String="",
  pwdhash: String="", role: String="", session: String="", sessionTs: Long= -1,
  lastSeen: Long= -1) extends Vertex {

  override def clone(newid: String) = this

  override def toString: String = name
}