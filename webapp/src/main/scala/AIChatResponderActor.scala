package com.graphbrain.webapp

import akka.actor.{Actor, Props}
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.{JsonContent, ResponseString}

import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.UserNode
import com.graphbrain.hgdb.ID
import com.graphbrain.nlp.SentenceParser

object AIChatResponderActor {
  case class Sentence(sentence: String, root: Vertex, user: UserNode, responder: Async.Responder[HttpResponse])
}

class AIChatResponderActor() extends Actor {
  import AIChatResponderActor._

  val sparser = new SentenceParser()

  override protected def receive = {
    case Sentence(sentence, root, user, responder) =>
      var replySentence = "Sorry, I don't understand."
      var goto = ""

      try {
        val parse = sparser.parseSentence(sentence, root, Option(user))

        if ((parse._1.length >= 1) && (parse._2.length >= 1) && (parse._3.length >= 1)) {
          val node1 = parse._1(0)
          val node2 = parse._3(0)
          val relation = parse._2(0).id.replace(" ", "_")

          //println("node1: " + node1.id)
          //println("node2: " + node2.id)
          //println("relation: " + relation)

          Server.store.createAndConnectVertices2(relation, Array(node1, node2), user.id)

          // force consesnsus re-evaluation of affected edge
          val edgeId = ID.edgeId(relation, Array(node1.id, node2.id))
          Server.consensusActor ! edgeId

          replySentence = "This fact was recorded: '" + sentence + "'"
          goto = root.id
        }
      }
      catch {
        case e => e.printStackTrace()
      }

      val replyMap = Map(("sentence" -> replySentence), ("goto" -> goto))
      responder.respond(JsonContent ~> ResponseString(JSONGen.json(replyMap)))
  }
}
