package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie

import com.graphbrain.utils.SimpleLog


object NodePlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with SimpleLog {
  def nodeResponse(id: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    val userNode = Server.getUser(cookies)
    val node = Server.store.get(id)
    Server.log(req, cookies, "NODE id: " + id)
    ldebug("NODE id: " + id, Console.CYAN)
    NodePage(Server.store, node, userNode, Server.prod, req, cookies).response
  }

  def rawResponse(id: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    val userNode = Server.getUser(cookies)
    val vertex = Server.store.get(id)
    Server.log(req, cookies, "RAW id: " + id)
    ldebug("RAW id: " + id, Console.CYAN)
    RawPage(vertex, userNode, req, cookies).response
  }

  def intent = {
    case req@GET(Path(Seg2("node" :: n :: Nil)) & Cookies(cookies)) =>
      nodeResponse(n, cookies, req)
    case req@GET(Path(Seg2("raw" :: n :: Nil)) & Cookies(cookies)) =>
      rawResponse(n, cookies, req)
  }
}
