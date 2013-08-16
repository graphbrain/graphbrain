package com.graphbrain.db

import java.net.URLEncoder

object ID {

  def sanitize(str: String): String = str.toLowerCase.replace("/", "_").replace(" ", "_")

  def parts(id: String) = id.split('/')

  def numberOfParts(id: String) = parts(id).length

  def namespace(id: String) = {
    val p = parts(id)
    p.slice(0, p.size - 1).reduceLeft(_ + "/" + _)
  }

  def lastPart(id: String) = {
    val p = parts(id)
    p(p.size - 1)
  }

  def humanReadable(id: String) = lastPart(id).toLowerCase.split("_").map(_.capitalize) mkString " "

  def isUserNode(idOrNs: String): Boolean = (parts(idOrNs)(0) == "user") && (numberOfParts(idOrNs) == 2)

  def isInUserSpace(idOrNs: String): Boolean = (parts(idOrNs)(0) == "user") && (numberOfParts(idOrNs) > 2)

  def isContextNode(idOrNs: String): Boolean = (numberOfParts(idOrNs) == 4) && (parts(idOrNs)(2) == "context")

  def isInContextSpace(idOrNs: String): Boolean = (numberOfParts(idOrNs) > 3) && (parts(idOrNs)(2) == "context")

  def isPersonal(idOrNs: String): Boolean = (parts(idOrNs)(0) == "user") &&
                                              (numberOfParts(idOrNs) > 3) &&
                                              (parts(idOrNs)(2) == "p")

  def isInUserGlobalSpace(idOrNs: String): Boolean = isInUserSpace(idOrNs) && !isPersonal(idOrNs)

  def isInSystemSpace(idOrNs: String): Boolean = parts(idOrNs)(0) == "sys"

  def globalToUser(idOrNs: String, userid: String) = {
    if (isInUserSpace(idOrNs))
      idOrNs
    else if (isUserNode(idOrNs))
      idOrNs
    else
      if ((numberOfParts(userid) > 0) && (parts(userid)(0) == "user")) {
        userid + "/" + idOrNs
      }
      else {
        "user/" + userid + "/" + idOrNs
      }
  }

  def userToGlobal(idOrNs: String) = {
    if (isInUserGlobalSpace(idOrNs) && (!isInContextSpace(idOrNs))) {
      val idParts = parts(idOrNs)
      val globalParts = idParts.slice(2, idParts.length)
      if (globalParts.length == 1) {
        globalParts(0)
      }
      else {
        globalParts.reduceLeft(_ + "/" + _)
      }
    }
    else {
      idOrNs
    }
  }

  def removeContext(idOrNs: String) = {
    if (isInContextSpace(idOrNs)) {
      val idParts = parts(idOrNs)

      val newParts = idParts.toList diff List(idParts(2), idParts(3))
      newParts.reduceLeft(_ + "/" + _)
    }
    else {
      idOrNs
    }
  }

  def setContext(idOrNs: String, contextId: String) =
    if (isUserNode(idOrNs) || isContextNode(idOrNs)) {
      if (contextId == "") idOrNs else contextId
    }
    else {
      (if (contextId == "") "" else contextId + "/") + userToGlobal(removeContext(idOrNs))
    }

  def ownerId(idOrNs: String): String = {    
    val tokens = parts(idOrNs)
    if (tokens(0) == "user")
      "user/" + tokens(1)  
    else
      ""
  }

  def contextId(idOrNs: String): String = {
    if (isInContextSpace(idOrNs)) {
      val idParts = parts(idOrNs)
      idParts(0) + "/" + idParts(1) + "/" + idParts(2) + "/" + idParts(3)
    }
    else {
      ""
    }
  }

  def relationshipId(edgeType:String, position: Int) = edgeType + "/" + position

  def userIdFromUsername(username: String) = "user/" + username

  def emailId(email: String) = "email/" + email.toLowerCase

  def idFromUsername(username: String) = {
    val usernameNoSpaces = username.replace(' ', '_')
    "user/" + URLEncoder.encode(usernameNoSpaces, "UTF-8")
  }

  def usergenerated_id(userName:String, thing:String) =
	  userName + "/" + thing

  def personalOwned_id(userName: String, thing: String, which: Int=1) =
    "user/" + userName + "/p/" + which.toString + "/" + sanitize(thing)

  def text_id(thing:String, which:Int=1):String =
	  which.toString + "/" + sanitize(thing)

  def relation_id(relation:String):String =
	  sanitize(relation)

  def relation_id(rel:String, node1ID:String, node2ID:String):String=
  {
    val tokens=List[String](rel)++Array[String](node1ID, node2ID)
    tokens.reduceLeft(_+ " " +_)
  }

  def reltype_id(relT:String, which:Int=1): String =
    "rtype/" + which.toString + "/" + sanitize(relT)

  def rule_id(rule_name: String): String =
    "rule/" + sanitize(rule_name)

  def wikipedia_id(wptitle:String):String={
    val title = wptitle.toLowerCase.replace(" ", "_")
    "wikipedia/"+title
  }
}
