package com.graphbrain.hgdb

import java.net.URLEncoder
import java.security.SecureRandom
import java.math.BigInteger

import org.mindrot.BCrypt


case class UserNode(store: VertexStore, id: String="", username: String="", name: String="",
  email: String="", pwdhash: String="", role: String="", session: String="",
  sessionTs: Long= -1, lastSeen: Long= -1, contexts: List[ContextNode]=null, summary: String="") extends Textual {

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
    if (contexts != null) {
      val contextStr = contexts.foldLeft("") {
        (x, y) => x + " " + y.id + " " + y.access
      }
      updater.setString("contexts", contextStr)
    }
    template.update(updater)
    store.onPut(this)
    this
  }

  override def clone(newid: String) = this

  override def toString: String = name

  override def updateSummary: Textual = UserNode(store, id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, contexts, generateSummary)

  override def raw: String = {
    val contextStr = if (contexts != null) {
      contexts.foldLeft("") {
        (x, y) => x + " " + y.name
      }
    }
    else {
      ""
    }

    "type: " + "user<br />" +
    "username: " + username + "<br />" +
    "name: " + name + "<br />" +
    "role: " + role + "<br />" +
    "lastSeen: " + lastSeen + "<br />" +
    "contexts: " + contextStr + "<br />"
  }
}