package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._

import com.graphbrain.db.{EntityNode, EdgeType, SearchInterface, Edge}
import com.graphbrain.utils.JSONGen


object DisambigPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def intent = {
    case req@POST(Path("/disambig") & Params(params)) => {
      val text = params("text")(0)
      val mode = params("mode")(0)
      val rel = params("rel")(0)
      val participantIds = params("participants")(0)
      val pos = params("pos")(0)
      

      val si = new SearchInterface(WebServer.graph)
      val results = si.query(text.toLowerCase)
      
      val resultsList: Seq[List[String]] = for (id <- results)
      yield List(id, WebServer.graph.description(id))
      
      val json = Map("count" -> results.size, "results" -> resultsList, "mode" -> mode, "text" -> text,
        "rel" -> rel, "participants" -> participantIds, "pos" -> pos)
      JsonContent ~> ResponseString(JSONGen.json(json))
    }
    case req@POST(Path("/disambig_create") & Params(params) & Cookies(cookies)) => {
      val userNode = WebServer.getUser(cookies)

      val mode = params("mode")(0)
      val text = params("text")(0)
      val rel = params("rel")(0)
      val participants = params("participants")(0)
      val pos = params("pos")(0).toInt

      val participantIds = participants.split(" ")

      // undo previous connection
      if (mode == "change") {
        WebServer.graph.remove(Edge.fromParticipants(rel, participantIds), userNode.id)
        // force consesnsus re-evaluation of affected edge
        val edge = Edge.fromParticipants(rel, participantIds)
        WebServer.consensusActor ! edge
      }

      // define new node
      val si = new SearchInterface(WebServer.graph)
      val results = si.query(text.toLowerCase)
      val number = results.size + 1
      val newNode = EntityNode.fromNsAndText(number.toString, text)

      // create revised edge
      val participantNodes = (for (pid <- participantIds) yield WebServer.graph.get(pid)).toArray
      participantNodes(pos) = newNode
      val relType = new EdgeType(rel)
      WebServer.graph.createAndConnectVertices(Array(relType) ++ participantNodes, userNode.id)

      // force consesnsus re-evaluation of affected edge
      val edge = Edge.fromParticipants(rel, participantNodes.map(_.id))
      WebServer.consensusActor ! edge

      WebServer.log(req, cookies, "DISAMBIG_CREATE mode: " + mode + "; text: " + text + "; rel: " + rel + "; participants:" + participants + "; pos: " + pos)

      JsonContent ~> ResponseString(JSONGen.json(""))
    }
    case req@POST(Path("/disambig_change") & Params(params) & Cookies(cookies)) => {
      val userNode = WebServer.getUser(cookies)

      val mode = params("mode")(0)
      val rel = params("rel")(0)
      val participants = params("participants")(0)
      val pos = params("pos")(0).toInt
      val changeTo = params("changeto")(0)

      val participantIds = participants.split(" ")

      // undo previous connection
      if (mode == "change") {
        WebServer.graph.remove(Edge.fromParticipants(rel, participantIds), userNode.id)
        // force consesnsus re-evaluation of affected edge
        val edge = Edge.fromParticipants(rel, participantIds)
        WebServer.consensusActor ! edge
      }

      // create revised edge
      val newParticipantIds = participantIds.toArray
      newParticipantIds(pos) = changeTo
      WebServer.graph.put(Edge.fromParticipants(rel, newParticipantIds), userNode.id)

      // force consesnsus re-evaluation of affected edge
      val edge = Edge.fromParticipants(rel, newParticipantIds)
      WebServer.consensusActor ! edge

      WebServer.log(req, cookies, "DISAMBIG_CHANGE mode: " + mode + "; rel: " + rel + "; participants:" + participants + "; pos: " + pos + "; changeTo: " + changeTo)

      JsonContent ~> ResponseString(JSONGen.json(""))
    }
  }
}
