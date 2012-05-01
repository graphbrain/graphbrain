package com.graphbrain.hgdb


import java.net.URLEncoder
import org.mindrot.BCrypt


trait UserManagement extends VertexStoreInterface {
  
  def idFromUsername(username: String) = {
  	val usernameNoSpaces = username.replace(' ', '_')
  	"user/" + URLEncoder.encode(usernameNoSpaces, "UTF-8")
  }

  def idFromEmail(email: String) = {
  	"email/" + URLEncoder.encode(email, "UTF-8")
  }

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

  def checkPassword(user: UserNode, candidate: String): Boolean = BCrypt.checkpw(candidate, user.pwdhash)

  def createUser(username: String, name: String, email: String, password: String, role: String): UserNode = {
  	val id = idFromUsername(username)
  	val pwdhash = BCrypt.hashpw(password, BCrypt.gensalt())
  	val userNode = put(UserNode(id=id, username=username, name=name, email=email, pwdhash=pwdhash, role=role, creationTs= -1))
	val emailId = idFromEmail(email)
  	put(UserEmailNode(id=emailId, username=username, email=email))

  	// create "is -> user" relationship
  	// make user "user" node exists
  	if (!exists("user")) {
  		put(TextNode("user", "user"))
  	}
  	addrel("is", Array(id, "user"))
  	userNode.asInstanceOf[UserNode]
  }
}