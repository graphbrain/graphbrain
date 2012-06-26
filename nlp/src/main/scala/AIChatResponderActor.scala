package com.graphbrain.nlp

import akka.actor.{ Actor, Props }
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.{ PlainTextContent, ResponseString }

import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.UserNode

object AIChatResponderActor {
  case class Sentence(sentence: String, root: Vertex, user: UserNode, responder: Async.Responder[HttpResponse])
}

class AIChatResponderActor() extends Actor {
  import AIChatResponderActor._

  override protected def receive = {
    case Sentence(sentence, root, user, responder) =>
      responder.respond(PlainTextContent ~> ResponseString(sentence))
      context.stop(self)
  }
}