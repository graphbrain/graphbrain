package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie

import com.codahale.logula.Logging


object NodeActionsPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with Logging {
  var errorMessage: String = ""

  def nodeActionResponse(id: String, params: Map[String, Seq[String]], cookies: Map[String, Any], req: HttpRequest[Any]) = {
    errorMessage = ""

    val op = params("op")(0)

    if (op == "remove") {
      removeLinkOrNode(params, cookies, req)
    }

    val userNode = Server.getUser(cookies)
    val node = Server.store.get(id)
    log.info(Server.realIp(req) + " NODE " + id)
    NodePage(Server.store, node, userNode, Server.prod, req, cookies, errorMessage).response
  }

  def removeLinkOrNode(params: Map[String, Seq[String]], cookies: Map[String, Any], req: HttpRequest[Any]) = {
    val userNode = Server.getUser(cookies)
    val nodeId = params("node")(0)
    val origId = params("orig")(0)
    val link = params("link")(0).replace(" ", "_")
    val targId = params("targ")(0)
    val linkOrNode = params("linkOrNode")(0)

    if (linkOrNode == "link") {
      // check permissions
      if ((Server.store.brainOwner(origId) == userNode.id) ||
          (Server.store.brainOwner(targId) == userNode.id)) {

        Server.store.delrel(link, Array(origId, targId))
        log.info(Server.realIp(req) + " REMOVE EDGE username: " + userNode.username + "; origId: " + origId + "; link: " + link + "; targId" + targId)
      }
      else {
        errorMessage = "You do not have perissons to remove this connection."
        log.info(Server.realIp(req) + " PERMISSION DENIED to REMOVE EDGE username: " + userNode.username + "; origId: " + origId + "; link: " + link + "; targId" + targId)
      }
    }
    else {
      // check permissions
      if (Server.store.brainOwner(nodeId) == userNode.id) {
        Server.store.removeVertexAndEdgesUI(Server.store.get(nodeId))
        log.info(Server.realIp(req) + " REMOVE NODE username: " + userNode.username + "; nodeId: " + nodeId)
      }
      else {
        errorMessage = "You do not have perissons to remove this item."
        log.info(Server.realIp(req) + " PERMISSION DENIED to REMOVE NODE username: " + userNode.username + "; nodeId: " + nodeId) 
      }
    }
  }

  def intent = {
    case req@POST(Path(Seg("node" :: n1 :: Nil)) & Params(params) & Cookies(cookies)) =>
      nodeActionResponse(n1, params, cookies, req)
    case req@POST(Path(Seg("node" :: n1 :: n2 :: Nil)) & Params(params) & Cookies(cookies)) =>
      nodeActionResponse(n1 + "/" + n2, params, cookies, req)
    case req@POST(Path(Seg("node" :: n1 :: n2 :: n3 :: Nil)) & Params(params) & Cookies(cookies)) =>
      nodeActionResponse(n1 + "/" + n2 + "/" + n3, params, cookies, req)
    case req@POST(Path(Seg("node" :: n1 :: n2 :: n3 :: n4 :: Nil)) & Params(params) & Cookies(cookies)) =>
      nodeActionResponse(n1 + "/" + n2 + "/" + n3 + "/" + n4, params, cookies, req)
    case req@POST(Path(Seg("node" :: n1 :: n2 :: n3 :: n4 :: n5 :: Nil)) & Params(params) & Cookies(cookies)) =>
      nodeActionResponse(n1 + "/" + n2 + "/" + n3 + "/" + n4 + "/" + n5, params, cookies, req)
  }
}