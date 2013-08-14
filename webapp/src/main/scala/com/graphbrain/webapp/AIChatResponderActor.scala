package com.graphbrain.webapp

import akka.actor.Actor
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.JsonContent

import com.graphbrain.db._
import com.graphbrain.utils.JSONGen
import com.graphbrain.nlp.SentenceParser

import com.graphbrain.utils.SimpleLog
import com.graphbrain.nlp.GraphResponse
import scala.Some
import unfiltered.response.ResponseString
import scala.collection.mutable


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

  override def receive = {
    case Sentence(sentence, root, user, responder) =>
      var replySentence = "Sorry, I don't understand."
      var goto = ""
      var newEdges = mutable.Set[String]()
      newEdges += ""

      try {
        val responses = sparser.parseSentenceGeneral(sentence, root, Option(user))
        println(responses)

        if (responses.length > 0) {

          val topResponse = responses(0)
          println(topResponse)
          topResponse match {
            case r: GraphResponse =>
              val parses = r.hypergraphList

              val topParse = parses(0)

              val nodes = topParse._1.map(_._1).toArray
              val nodeIds = nodes.map(n => n.id)
              println("\n\n\n ************* " + nodeIds + "\n************\n\n\n")
              val relation = topParse._2.id.replace(" ", "_")

              //ldebug("node1: " + node1.id + "\nnode2: " + node2.id + "\nrelation: " + relation, Console.RED)

              val relType = EdgeType(relation)
              WebServer.graph.createAndConnectVertices(Array(relType) ++ nodes, user.id)

              val edge = Edge.fromParticipants(relation, nodeIds)
              println("KKKKKKKKKKKKKKKKKK >> " + edge.toString)
              newEdges += edge.toGlobal.toString

              // force consesnsus re-evaluation of affected edge
              WebServer.consensusActor ! edge

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
                val firstNode = fact._1
                val extraFact = fact._2
                extraFact match {
                  case Some(f) =>
                    val nodes = (firstNode :: f._1).toArray
                    val nodeIds = nodes.map(_.id)
                    val relation = f._2.id.replace(" ", "_")

                    val relType = EdgeType(relation)
                    WebServer.graph.createAndConnectVertices(Array(relType) ++  nodes, user.id)

                    val edge = Edge.fromParticipants(relation, nodeIds)
                    newEdges += edge.toGlobal.toString

                    // force consesnsus re-evaluation of affected edge
                    WebServer.consensusActor ! edge
                }
              }
            case _ =>

          }
        }
      }
      catch {
        case e: Throwable => e.printStackTrace()
      }

      val replyMap = Map("sentence" -> replySentence, "goto" -> goto, "newedges" -> newEdges)
      responder.respond(JsonContent ~> ResponseString(JSONGen.json(replyMap)))
  }
}
