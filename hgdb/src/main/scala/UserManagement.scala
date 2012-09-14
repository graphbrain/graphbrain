package com.graphbrain.hgdb


import java.net.URLEncoder
import java.security.SecureRandom
import java.math.BigInteger

import org.mindrot.BCrypt


trait UserManagement extends VertexStore {
  
  private val random: SecureRandom = new SecureRandom

  private def associateEmailToUsername(email: String, username: String) = {
    val template = backend.tpEmail
    val updater = template.createUpdater(email)
    updater.setString("username", username)
    template.update(updater)
  }


  private def usernameByEmail(email: String): String = {
    val res = backend.tpEmail.queryColumns(email)
    res.getString("username")
  }

  private def idFromEmail(email: String): String = idFromUsername(usernameByEmail(email))

  private def usernameExists(username: String) = exists(idFromUsername(username))

  private def emailExists(email: String) = {
    val res = backend.tpEmail.queryColumns(email)
    res.hasResults 
  }

  private def genSessionId: String = new BigInteger(130, random).toString(32)

  private def idFromUsername(username: String) = {
  	val usernameNoSpaces = username.replace(' ', '_')
  	"user/" + URLEncoder.encode(usernameNoSpaces, "UTF-8")
  }

  def findUser(login: String): UserNode = {
  	if (exists(idFromUsername(login))) {
  	  getUserNode(idFromUsername(login))
  	}
  	else if (exists(idFromEmail(login))) {
  	  getUserNode(idFromEmail(login))
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
      associateEmailToUsername(email, username)
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