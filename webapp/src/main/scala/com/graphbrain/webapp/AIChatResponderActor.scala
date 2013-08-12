package com.graphbrain.webapp


import scala.collection.mutable.Set

import akka.actor.Actor
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.{JsonContent, ResponseString}

import com.graphbrain.db.Vertex
import com.graphbrain.db.Edge
import com.graphbrain.db.Textual
import com.graphbrain.db.UserNode
import com.graphbrain.db.ID
import com.graphbrain.utils.JSONGen
import com.graphbrain.nlp.SentenceParser
import com.graphbrain.nlp.GraphResponse

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
        "<br />&nbsp;&nbsp;&nbsp;&nbsp;Using: " + t + " " + Textual.generateSummary(t.id, WebServer.graph) + ". <a href='#' class='aichat_action' onclick='" + onclick + "'>Did you mean another " + t + "?</a>"
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
        println(responses)

        if (responses.length > 0) {

          val topResponse = responses(0);
          println(topResponse)
          topResponse match {
            case r: GraphResponse =>
              val parses = r.hypergraphList

              val topParse = parses(0);

              val contextId = ID.contextId(root.id)
              val nodes = topParse._1.map(_._1).map(_.setContext(contextId)).toArray
              val nodeIds = nodes.map(n => ID.setContext(n.id, contextId)).toList
              println("\n\n\n ************* " + nodeIds + "\n************\n\n\n")
              val relation = topParse._2.id.replace(" ", "_")

              //ldebug("node1: " + node1.id + "\nnode2: " + node2.id + "\nrelation: " + relation, Console.RED)

              Server.store.createAndConnectVertices2(relation, nodes, user.id)

              val edge = Edge(relation, nodeIds)
              println("KKKKKKKKKKKKKKKKKK >> " + edge.toString)
              newEdges += edge.toGlobal.toString

              // force consesnsus re-evaluation of affected edge
              Server.consensusActor ! edge

              //TODO: fix undoFunctionCall to support facts with any number of participants
              val undoFunctionCall = "undoFact('" + relation + "', '" + nodes(0).id + " " + nodes(1).id + "')"
              
              replySentence = "Fact recorded: '" + sentence + "'. <a href=\"#\" class=\"aichat_action\" onclick=\"" + undoFunctionCall + "\">Want to undo?</a>"
              
              for (i <- 0 until nodes.length) {
                replySentence += disambiguationMessage(relation, nodes, i)
              }
                
              goto = root.id

              // Create extra facts
              val facts = topParse._1
              for (fact <- facts) {
                val firstNode = fact._1.setContext(contextId)
                val extraFact = fact._2
                extraFact match {
                  case Some(f) =>
                    val nodes = (firstNode :: f._1.map(_.setContext(contextId))).toArray
                    val nodeIds = nodes.map(_.id).toList
                    val relation = f._2.id.replace(" ", "_")

                    Server.store.createAndConnectVertices2(relation, nodes, user.id)

                    val edge = Edge(relation, nodeIds)
                    newEdges += edge.toGlobal.toString

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
