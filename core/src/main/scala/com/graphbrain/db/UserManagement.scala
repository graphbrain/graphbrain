package com.graphbrain.db

trait UserManagement extends Graph {

  def getUserNode(id: String) =
    get(id) match {
      case u: UserNode => u
      case _ => null
    }

  protected def idFromEmail(email: String) = {
    val userName = back.usernameByEmail(email)
    if (userName == null)
      null
    else
      ID.idFromUsername(userName)
  }

  def usernameExists(username: String) = exists(ID.idFromUsername(username))

  def emailExists(email: String) = back.usernameByEmail(email) != null

  def findUser(login: String) = {
    if (exists(ID.idFromUsername(login))) {
      getUserNode(ID.idFromUsername(login))
    }
    else {
      val uid = idFromEmail(login)
      if (uid == null)
        null
      else if (exists(uid))
        getUserNode(idFromEmail(login))
      else
        null
    }
  }

  def getUserNodeByUsername(username: String) =
    if (exists(ID.idFromUsername(username)))
      getUserNode(ID.idFromUsername(username))
    else
      null

  def createUser(username: String, name: String, email: String, password: String, role: String) = {
    val userNode = UserNode.create(username, name, email, password, role)
    back.put(userNode)
    if (!email.equals(""))
      back.associateEmailToUsername(email, username)

    userNode
  }

  def attemptLogin(login: String, password: String): UserNode = {
    val userNode = findUser(login)

    // user does not exist
    if (userNode == null)
      return null

    // password is incorrect
    if (!userNode.checkPassword(password))
      return null

    // ok, create new session
    val un = userNode.newSession
    update(un)
    un
  }

  def forceLogin(login: String): UserNode = {
    val userNode = findUser(login)

    // user does not exist
    if (userNode == null)
      return null

    // ok, create new session
    val un = userNode.newSession
    update(un)
    un
  }

  def allUsers = back.listByType(VertexType.User).map(f = v => v match {
    case u: UserNode => u
    case _ => null
  })
}