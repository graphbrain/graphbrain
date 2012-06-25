package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie
import com.codahale.logula.Logging
import akka.actor.{ Actor, Props }

import com.graphbrain.nlp.AIChatResponderActor

object AIChatPlan extends async.Plan with ServerErrorResponse with Logging {
  def intent = {
    case req@GET(Path(Seg("xpto" :: Nil)) & Cookies(cookies)) =>
      val responderActor =
        Server.actorSystem.actorOf(Props(new AIChatResponderActor(req)))
      responderActor ! AIChatResponderActor.Sentence("actor responding to request")
  }
}