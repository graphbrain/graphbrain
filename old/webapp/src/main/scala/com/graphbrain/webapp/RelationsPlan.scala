package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.netty._
import akka.actor.Props

object RelationsPlan extends async.Plan with ServerErrorResponse {
  val responderActor = WebServer.actorSystem.actorOf(Props(new RelationsResponderActor()))

  def intent = {
    case req@POST(Path("/rel") & Params(params) & Cookies(cookies)) =>
      val userNode = WebServer.getUser(cookies)
      val rel = params("rel")(0)
      val pos = params("pos")(0).toInt
      val rootId = params("rootId")(0)
      val root = WebServer.graph.get(rootId)
      responderActor ! RelationsResponderActor.Relation(rel, pos, root, userNode, req)

      WebServer.log(req, cookies, "REL rootId: " + rootId + "; edgeType: " + rel + "; pos: " + pos)
  }
}
