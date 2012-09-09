package com.graphbrain.hgdb


import java.net.URLEncoder
import java.security.SecureRandom
import java.math.BigInteger

import org.mindrot.BCrypt


trait UserManagement extends VertexStore {
  
  private val random: SecureRandom = new SecureRandom

  def genSessionId: String = new BigInteger(130, random).toString(32)

  def idFromUsername(username: String) = {
  	val usernameNoSpaces = username.replace(' ', '_')
  	"user/" + URLEncoder.encode(usernameNoSpaces, "UTF-8")
  }

  def idFromEmail(email: String) = {
  	"email/" + URLEncoder.encode(email, "UTF-8")
  }

  def usernameExists(username: String): Boolean = exists(idFromUsername(username))

  def emailExists(email: String): Boolean = exists(idFromEmail(email))

  def findUser(login: String): UserNode = {
  	if (exists(idFromUsername(login))) {
  	  getUserNode(idFromUsername(login))
  	}
  	else if (exists(idFromEmail(login))) {
  	  val emailNode = getUserEmailNode(idFromEmail(login))
  	  getUserNode(idFromUsername(emailNode.username))
  	}
  	else {
  	  null
    }
  }

  def getUserNodeByUsername(username: String): UserNode = {
    if (exists(idFromUsername(username))) {
      getUserNode(idFromUsername(username))
    }
    else {
      null
    }
  }

  def checkPassword(user: UserNode, candidate: String): Boolean = BCrypt.checkpw(candidate, user.pwdhash)

  def checkSession(user: UserNode, candidate: String): Boolean = user.session == candidate

  def createUser(username: String, name: String, email: String, password: String, role: String): UserNode = {
  	val id = idFromUsername(username)
  	val pwdhash = BCrypt.hashpw(password, BCrypt.gensalt())
  	val userNode = put(UserNode(id=id, username=username, name=name, email=email, pwdhash=pwdhash, role=role))
	  if (email != "") {
      val emailId = idFromEmail(email)
  	  put(UserEmailNode(id=emailId, username=username, email=email))
    }

  	// create "is -> user" relationship
  	// make user "user" node exists
  	if (!exists("user")) {
  		put(TextNode("user", "user"))
  	}
  	userNode.asInstanceOf[UserNode]
  }

  def attemptLogin(login: String, password: String): UserNode = {
    val userNode = findUser(login)

    // user does not exist
    if (userNode == null) {
      return null
    }
    
    // password is incorrect
    if (!checkPassword(userNode, password)) {
      return null
    }

    // ok, create new session and return it
    val session = genSessionId
    val updatedUser = userNode.copy(session=session)
    update(updatedUser)
    updatedUser
  }
}
