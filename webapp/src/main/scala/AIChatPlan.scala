package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie
import akka.actor.Actor
import akka.actor.Props


object AIChatPlan extends async.Plan with ServerErrorResponse {
  val responderActor = Server.actorSystem.actorOf(Props(new AIChatResponderActor()))

  def intent = {
    case req@POST(Path("/ai") & Params(params) & Cookies(cookies)) =>
      val userNode = Server.getUser(cookies)
      val sentence = params("sentence")(0)
      val rootId = params("rootId")(0)
      val root = Server.store.get(rootId)
      responderActor ! AIChatResponderActor.Sentence(sentence, root, userNode, req)
  }
}
