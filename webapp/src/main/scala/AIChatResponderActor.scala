package com.graphbrain.webapp


import scala.collection.mutable.Set

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

import com.graphbrain.utils.SimpleLog


object AIChatResponderActor {
  case class Sentence(sentence: String, root: Vertex, user: UserNode, responder: Async.Responder[HttpResponse])
}

class AIChatResponderActor() extends Actor with SimpleLog {
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
      var newEdges = Set[String]()
      newEdges += ""

      try {
        val responses = sparser.parseSentenceGeneral(sentence, root, Option(user))

        if (responses.length > 0) {

          val topResponse = responses(0);
          println(topResponse)
          topResponse match {
            case r: GraphResponse =>
              val parses = r.hypergraphList

              val topParse = parses(0);

              val nodes = topParse._1.map(_._1).toArray
              val nodeIds = nodes.map(_.id).toList
              //println(nodeIds)
              val relation = topParse._2.id.replace(" ", "_")

              //ldebug("node1: " + node1.id + "\nnode2: " + node2.id + "\nrelation: " + relation, Console.RED)

              Server.store.createAndConnectVertices2(relation, nodes, user.id)

              val edge = Edge(relation, nodeIds)
              newEdges += edge.toString

              // force consesnsus re-evaluation of affected edge
              Server.consensusActor ! edge

              //TODO: fix undoFunctionCall to support facts with any number of participants
              val undoFunctionCall = "undoFact('" + relation + "', '" + nodes(0).id + " " + nodes(1).id + "')"
              
              replySentence = "Fact recorded: '" + sentence + "'. <a href=\"#\" onclick=\"" + undoFunctionCall + "\">Want to undo?</a>"
              
              for (i <- 0 until nodes.length) {
                replySentence += disambiguationMessage(relation, nodes, i)
              }
                
              goto = root.id

              // Create extra facts
              val facts = topParse._1
              for (fact <- facts) {
                val firstNode = fact._1
                val extraFact = fact._2
                extraFact match {
                  case Some(f) =>
                    val nodes = (firstNode :: f._1).toArray
                    val nodeIds = nodes.map(_.id).toList
                    val relation = f._2.id.replace(" ", "_")

                    Server.store.createAndConnectVertices2(relation, nodes, user.id)

                    val edge = Edge(relation, nodeIds)
                    newEdges += edge.toString

                    // force consesnsus re-evaluation of affected edge
                    Server.consensusActor ! edge                    
                }
              }
            case _ =>

          }
        }
      }
      catch {
        case e => e.printStackTrace()
      }

      val replyMap = Map(("sentence" -> replySentence), ("goto" -> goto), ("newedges" -> newEdges))
      responder.respond(JsonContent ~> ResponseString(JSONGen.json(replyMap)))
  }
}
