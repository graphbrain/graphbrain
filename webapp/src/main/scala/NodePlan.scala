package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie

import com.codahale.logula.Logging


object NodePlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse with Logging {
  def nodeResponse(id: String, cookies: Map[String, Any], req: HttpRequest[Any]) = {
    val userNode = Server.getUser(cookies)
    val node = Server.store.get(id)
    log.info(Server.realIp(req) + " NODE " + id)
    NodePage(Server.store, node, userNode, Server.prod, req).response
  }

  def intent = {
    case req@GET(Path(Seg("node" :: n1 :: Nil)) & Cookies(cookies)) =>
      nodeResponse(n1, cookies, req)
    case req@GET(Path(Seg("node" :: n1 :: n2 :: Nil)) & Cookies(cookies)) =>
      nodeResponse(n1 + "/" + n2, cookies, req)
    case req@GET(Path(Seg("node" :: n1 :: n2 :: n3 :: Nil)) & Cookies(cookies)) =>
      nodeResponse(n1 + "/" + n2 + "/" + n3, cookies, req)
    case req@GET(Path(Seg("node" :: n1 :: n2 :: n3 :: n4 :: Nil)) & Cookies(cookies)) =>
      nodeResponse(n1 + "/" + n2 + "/" + n3 + "/" + n4, cookies, req)
    case req@GET(Path(Seg("node" :: n1 :: n2 :: n3 :: n4 :: n5 :: Nil)) & Cookies(cookies)) =>
      nodeResponse(n1 + "/" + n2 + "/" + n3 + "/" + n4 + "/" + n5, cookies, req)
  }
}