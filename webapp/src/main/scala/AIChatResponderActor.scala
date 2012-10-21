package com.graphbrain.webapp

import akka.actor.{Actor, Props}
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.{JsonContent, ResponseString}

import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.Textual
import com.graphbrain.hgdb.UserNode
import com.graphbrain.hgdb.ID
import com.graphbrain.nlp.SentenceParser
import com.graphbrain.nlp.ResponseType
import com.graphbrain.nlp.GraphResponse
import com.graphbrain.nlp.HardcodedResponse
import com.graphbrain.nlp.SearchResponse
import com.graphbrain.nlp.QuestionFactResponse

object AIChatResponderActor {
  case class Sentence(sentence: String, root: Vertex, user: UserNode, responder: Async.Responder[HttpResponse])
}

class AIChatResponderActor() extends Actor {
  import AIChatResponderActor._

  val sparser = new SentenceParser()

  private def disambiguationMessage(rel: String, participants: Array[Vertex], pos: Int) = {
    val node = participants(pos)
    val participantIds = participants.foldLeft("")((a, b) => a + "\"" + b.id + "\",")
    node match {
      case t: Textual => {
        // TODO: deal with change (edge didn't exist before) vs add (edge already exists)
        val onclick = "aiChatDisambiguate(\"change\",\"" + t + "\",\"" + rel + "\",[" + participantIds + "]," + pos + ");"
        "<br />&nbsp;&nbsp;&nbsp;&nbsp;Using: " + t + " " + t.generateSummary + ". <a href='#' onclick='" + onclick + "'>Did you mean another " + t + "?</a>"
      }
      case _ => ""
    }
  }

  override protected def receive = {
    case Sentence(sentence, root, user, responder) =>
      var replySentence = "Sorry, I don't understand."
      var goto = ""

      try {
        val responses = sparser.parseSentenceGeneral(sentence, root, Option(user))

        if (responses.length > 0) {

          val topResponse = responses(0);
          println(topResponse)
          topResponse match {
            case r: GraphResponse =>
              val parses = r.hypergraphList
              val topParse = parses(0);
              //Check it's a 2-participant relationship
              if(topParse._1.length == 2) {
                val node1 = topParse._1(0)
                val node2 = topParse._1(1)
                val relation = topParse._2.id.replace(" ", "_")

                println("node1: " + node1.id)
                println("node2: " + node2.id)
                println("relation: " + relation)

                Server.store.createAndConnectVertices2(relation, Array(node1, node2), user.id)

                // force consesnsus re-evaluation of affected edge
                val edge = Edge(relation, List(node1.id, node2.id))
                Server.consensusActor ! edge

                val undoFunctionCall = "undoFact('" + relation + "', '" + node1.id + " " + node2.id + "')"
                replySentence = "Fact recorded: '" + sentence + "'. <a href=\"#\" onclick=\"" + undoFunctionCall + "\">Want to undo?</a>"
                replySentence += disambiguationMessage(relation, Array(node1, node2), 0)
                replySentence += disambiguationMessage(relation, Array(node1, node2), 1)
                
                goto = root.id
              }
            case _ =>

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
