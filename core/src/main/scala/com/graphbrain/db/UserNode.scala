package com.graphbrain.db

import org.mindrot.jbcrypt.BCrypt
import java.math.BigInteger
import com.graphbrain.utils.RandUtils


case class UserNode(override val id: String,
                    username: String,
                    name: String,
                    email: String,
                    pwdhash: String,
                    role: String="",
                    session: String="",
                    sessionTs: Long= -1,
                    lastSeen: Long= -1,
                    override val summary: String="",
                    override val degree: Int = 0,
                    override val ts: Long = -1)
  extends Textual(id, summary, degree, ts) {

  def this(id: String, map: Map[String, String]) =
    this(id,
      map("username"),
      map("name"),
      map("email"),
      map("pwdhash"),
      map("role"),
      map("session"),
      map("sessionTs").toLong,
      map("lastSeen").toLong,
      map("summary"),
      map("degree").toInt,
      map("ts").toLong)

  override def extraMap = Map("username" -> username,
                              "name" -> name,
                              "email" -> email,
                              "pwdhash" -> pwdhash,
                              "role" -> role,
                              "session" -> session,
                              "sessionTs" -> sessionTs.toString,
                              "lastSeen" -> lastSeen.toString,
                              "summary" -> summary)

  override def setId(newId: String): Vertex = copy(id=newId)

  override def setDegree(newDegree: Int): Vertex = copy(degree=newDegree)

  override def setTs(newTs: Long): Vertex = copy(ts=newTs)

  def newSession = copy(session= new BigInteger(130, RandUtils.secRand).toString(32))

  def checkPassword(candidate: String) = BCrypt.checkpw(candidate, pwdhash)

  def checkSession(candidate: String) = session == candidate

  override def toString: String = name

  //override def updateSummary: Textual = UserNode(id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, generateSummary)

  override def raw: String = {
    "type: " + "user<br />" +
    "username: " + username + "<br />" +
    "name: " + name + "<br />" +
    "role: " + role + "<br />" +
    "lastSeen: " + lastSeen + "<br />"
  }
}

object UserNode {
  def create(username: String, name: String, email: String, password: String, role: String) = {
    val id = ID.userIdFromUsername(username)
    val pwdhash = BCrypt.hashpw(password, BCrypt.gensalt())
    UserNode(id, username, name, email, pwdhash, role)
  }
}