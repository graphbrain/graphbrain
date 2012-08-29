package com.graphbrain.hgdb


object ID {

  def sanitize(str: String): String = str.toLowerCase.replace("/", "_").replace(" ", "_")

  def parts(id: String) = id.split('/').filter(p => p != "")

  def numberOfParts(id: String) = parts(id).length

  def namespace(id: String) = {
    val p = parts(id)
    p.slice(0, p.size - 1).reduceLeft(_ + "/" + _)
  }

  def lastPart(id: String) = {
    val p = parts(id)
    p(p.size - 1)
  }

  def isInUserSpace(idOrNs: String): Boolean = (parts(idOrNs)(0) == "user") && (numberOfParts(idOrNs) > 2)

  def isUserNode(idOrNs: String): Boolean = (parts(idOrNs)(0) == "user") && (numberOfParts(idOrNs) == 2)

  def isInSystemSpace(idOrNs: String): Boolean = parts(idOrNs)(0) == "sys"

  def globalToUser(idOrNs: String, userid: String) = {
    if (isInUserSpace(idOrNs))
      idOrNs
    else if (isUserNode(idOrNs))
      idOrNs
    else
      userid + "/" + idOrNs
  }

  def userToGlobal(idOrNs: String) = {
    if (isInUserSpace(idOrNs)) {
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

  def ownerId(idOrNs: String): String = {
    val idParts = parts(idOrNs)
    idParts(0) + "/" + idParts(1)
  }

  def edgeId(etype:String, participants: Array[String]) = (List[String](etype) ++ participants).reduceLeft(_ + " " + _)

  def edgeSetId(vertexId: String, edgeId: String) = {
    val pos = Edge.participantIds(edgeId).indexOf(vertexId)
    vertexId + "/" + pos + "/" + Edge.edgeType(edgeId)
  }

  def extraId(id: String, pos: Int) = {
    if (pos == 0)
      id
    else
      id + "/" + pos
  }

  def globalToUserEdge(edgeId: String, userId: String) = {
    val etype = Edge.edgeType(edgeId)
    val pids = Edge.participantIds(edgeId)

    etype + " " +
    (for (pid <- pids) yield globalToUser(pid, userId)).reduceLeft(_ + " " + _)
  }
  
  def userToGlobalEdge(edgeId: String) = {
    val etype = Edge.edgeType(edgeId)
    val pids = Edge.participantIds(edgeId)

    etype + " " +
    (for (pid <- pids) yield userToGlobal(pid)).reduceLeft(_ + " " + _)
  }

  def negateEdge(edgeId: String) = "neg/" + edgeId

  def isPositiveEdge(id: String): Boolean = parts(id)(0) != "neg"

  def usergenerated_id(userName:String, thing:String) =
	  userName + "/" + thing

  def image_id(image_url:String) =
    "image/" + sanitize(image_url)
  
  def image_id(image_name:String, image_url:String="") =
	  "image/" + sanitize(image_name) + "/" + sanitize(image_url)

  def video_id(video_url:String) =
    "video/" +  sanitize(video_url)

  def video_id(video_name:String, video_url:String="") =
	  "video/" + sanitize(video_name) + "/" + sanitize(video_url)

  def text_id(thing:String, which:Int=1):String =
	  which.toString + "/" + sanitize(thing)

  def relation_id(relation:String):String =
	  sanitize(relation)

  def relation_id(rel:String, node1ID:String, node2ID:String):String=
  {
    val tokens=List[String](rel)++Array[String](node1ID, node2ID)
    return tokens.reduceLeft(_+ " " +_)
  }

  def reltype_id(relT:String, which:Int=1): String =
    "rtype/" + which.toString + "/" + sanitize(relT)

  def rule_id(rule_name: String): String =
    "rule/" + sanitize(rule_name) 


  def url_id(url:String):String =
    "web/"+sanitize(url)

  def wikipedia_id(wptitle:String):String={
    val title = wptitle.toLowerCase.replace(" ", "_")
    return "wikipedia/"+title
  }



}
