package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie


object NodePlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def nodeResponse(id: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    val userNode = Server.getUser(cookies)
    val node = Server.store.get(id)
    Server.log(req, cookies, "NODE id: " + id)
    NodePage(Server.store, node, userNode, Server.prod, req, cookies).response
  }

  def rawResponse(id: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    val userNode = Server.getUser(cookies)
    val vertex = Server.store.get(id)
    Server.log(req, cookies, "RAW id: " + id)
    RawPage(vertex, userNode, req, cookies).response
  }

  def intent = {
    case req@GET(Path(Seg2("node" :: n :: Nil)) & Cookies(cookies)) =>
      nodeResponse(n, cookies, req)
    case req@GET(Path(Seg2("raw" :: n :: Nil)) & Cookies(cookies)) =>
      rawResponse(n, cookies, req)
  }
}
