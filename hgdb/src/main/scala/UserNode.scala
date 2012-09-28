package com.graphbrain.hgdb


case class UserNode(store: VertexStore, id: String="", username: String="", name: String="",
  email: String="", pwdhash: String="", role: String="", session: String="",
  sessionTs: Long= -1, lastSeen: Long= -1, summary: String="") extends Textual {

  override def put(): Vertex = {
    val template = store.backend.tpUser
    val updater = template.createUpdater(id)
    updater.setString("username", username)
    updater.setString("name", name)
    updater.setString("email", email)
    updater.setString("pwdhash", pwdhash)
    updater.setString("role", role)
    updater.setString("session", session)
    updater.setLong("sessionTs", sessionTs)
    updater.setLong("lastSeen", lastSeen)
    template.update(updater)
    this
  }

  override def clone(newid: String) = this

  override def toString: String = name

  override def updateSummary: Textual = UserNode(store, id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, generateSummary)
}