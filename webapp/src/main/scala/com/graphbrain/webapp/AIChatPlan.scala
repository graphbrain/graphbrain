package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.netty._
import akka.actor.Props


object AIChatPlan extends async.Plan with ServerErrorResponse {
  val responderActor = WebServer.actorSystem.actorOf(Props(new AIChatResponderActor()))

  def intent = {
    case req@POST(Path("/ai") & Params(params) & Cookies(cookies)) =>
      val userNode = WebServer.getUser(cookies)
      val sentence = params("sentence")(0)
      val rootId = params("rootId")(0)
      val root = WebServer.graph.get(rootId)
      responderActor ! AIChatResponderActor.Sentence(sentence, root, userNode, req)

      WebServer.log(req, cookies, "AI CHAT sentence: " + sentence + "; rootId: " + rootId)
  }
}