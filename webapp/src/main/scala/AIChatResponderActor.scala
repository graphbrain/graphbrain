package com.graphbrain.webapp

import akka.actor.{Actor, Props}
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.{JsonContent, ResponseString}

import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.Edge
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
        val parse = sparser.parseSentenceGeneral(sentence, root, Option(user))

        //println(parse)

        if (parse.length > 0) {
          val topParse = parse(0);
          //Check it's a 2-participant relationship
          if(topParse._1.length == 2) {
            val node1 = topParse._1(0)
            val node2 = topParse._1(1)
            val relation = topParse._2.id.replace(" ", "_")

            //println("node1: " + node1.id)
            //println("node2: " + node2.id)
            //println("relation: " + relation)

            Server.store.createAndConnectVertices2(relation, Array(node1, node2), user.id)

            // force consesnsus re-evaluation of affected edge
            val edge = Edge(relation, List(node1.id, node2.id))
            Server.consensusActor ! edge

            replySentence = "This fact was recorded: '" + sentence + "'"
            goto = root.id

          }
        }
      }
      catch {
        case e => e.printStackTrace()
      }

      val replyMap = Map(("sentence" -> replySentence), ("goto" -> goto))
      responder.respond(JsonContent ~> ResponseString(JSONGen.json(replyMap)))
  }
}
