package com.graphbrain.webapp

import java.net.URLDecoder
import unfiltered.request._
import unfiltered.netty._

object NodePlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def nodeResponse(id: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    WebServer.log(req, cookies, "NODE id: " + id)
    val userNode = WebServer.getUser(cookies)
    val node = WebServer.graph.get(URLDecoder.decode(id, "UTF-8"))
    NodePage(WebServer.graph, node, userNode, WebServer.prod, req, cookies).response
  }

  def rawResponse(id: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    val userNode = WebServer.getUser(cookies)
    val vertex = WebServer.graph.get(id)
    WebServer.log(req, cookies, "RAW id: " + id)
    RawPage(vertex, userNode, req, cookies).response
  }

  def intent = {
    case req@GET(Path(Seg2("node" :: n :: Nil)) & Cookies(cookies)) =>
      nodeResponse(n, cookies, req)
    case req@GET(Path(Seg2("raw" :: n :: Nil)) & Cookies(cookies)) =>
      rawResponse(n, cookies, req)
  }
}