package com.graphbrain.nlp

import akka.actor.{ Actor, Props }
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.{ PlainTextContent, ResponseString }

import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.UserNode
import com.graphbrain.nlp.SentenceParser

object AIChatResponderActor {
  case class Sentence(sentence: String, root: Vertex, user: UserNode, responder: Async.Responder[HttpResponse])
}

class AIChatResponderActor() extends Actor {
  import AIChatResponderActor._

  val sparser = new SentenceParser()

  override protected def receive = {
    case Sentence(sentence, root, user, responder) =>
      val parse = sparser.parseSentence(sentence, root, Option(user))
      println("orig: " + parse._1)
      println("rel: " + parse._2)
      println("targ: " + parse._3)
      responder.respond(PlainTextContent ~> ResponseString(sentence))
  }
}