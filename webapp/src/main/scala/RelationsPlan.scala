package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie
import akka.actor.Actor
import akka.actor.Props

import com.graphbrain.utils.SimpleLog


object RelationsPlan extends async.Plan with ServerErrorResponse with SimpleLog {
  val responderActor = Server.actorSystem.actorOf(Props(new RelationsResponderActor()))

  def intent = {
    case req@POST(Path("/rel") & Params(params) & Cookies(cookies)) =>
      val userNode = Server.getUser(cookies)
      val rel = params("rel")(0)
      val pos = params("pos")(0).toInt
      val rootId = params("rootId")(0)
      val root = Server.store.get(rootId)
      responderActor ! RelationsResponderActor.Relation(rel, pos, root, userNode, req)

      Server.log(req, cookies, "REL rootId: " + rootId + "; edgeType: " + rel + "; pos: " + pos)
      ldebug("REL rootId: " + rootId + "; edgeType: " + rel + "; pos: " + pos)
  }
}
