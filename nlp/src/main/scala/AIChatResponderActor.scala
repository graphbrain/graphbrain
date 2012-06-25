package com.graphbrain.nlp

import akka.actor.{ Actor, Props }
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.{ PlainTextContent, ResponseString }


object AIChatResponderActor {
  case class Sentence(sentence: String)
}

class AIChatResponderActor(responder: Async.Responder[HttpResponse]) extends Actor {
  import AIChatResponderActor._

  override protected def receive = {
    case Sentence(sentence) =>
      responder.respond(PlainTextContent ~> ResponseString(sentence))
      context.stop(self)
  }
}