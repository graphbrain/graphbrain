package com.graphbrain.hgdb


case class UserNode(id: String="", username: String="", name: String="", email: String="",
  pwdhash: String="", role: String="", session: String="", sessionTs: Long= -1,
  lastSeen: Long= -1, summary: String="", store: VertexStore=null) extends Textual {

  override def clone(newid: String) = this

  override def toString: String = name

  override def updateSummary: Textual = UserNode(id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, generateSummary, store)
}