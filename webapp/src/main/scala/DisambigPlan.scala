package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.Cookie

import com.graphbrain.hgdb.SearchInterface
import com.graphbrain.hgdb.Edge


object DisambigPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {
  def intent = {
    case req@POST(Path("/disambig") & Params(params)) => {
      val text = params("text")(0)
      val mode = params("mode")(0)
      val rel = params("rel")(0)
      val participantIds = params("participants")(0)
      val pos = params("pos")(0)
      

      val si = new SearchInterface(Server.store)
      val results = si.query(text.toLowerCase)
      
      val resultsList: Seq[List[String]] = (for (id <- results)
        yield List(id, Server.store.get(id).description))
      
      val json = Map(("count" -> results.size), ("results" -> resultsList), ("mode" -> mode), ("text" -> text),
        ("rel" -> rel), ("participants" -> participantIds), ("pos" -> pos))
      ResponseString(JSONGen.json(json))
    }
    case req@POST(Path("/disambig_create") & Params(params) & Cookies(cookies)) => {
      val userNode = Server.getUser(cookies)

      val mode = params("mode")(0)
      val text = params("text")(0)
      val rel = params("rel")(0)
      val participants = params("participants")(0)
      val pos = (params("pos")(0)).toInt

      val participantIds = participants.split(" ").toList

      // undo previous connection
      if (mode == "change") {
        Server.store.delrel2(rel, participantIds, userNode.id)
        // force consesnsus re-evaluation of affected edge
        val edge = Edge(rel, participantIds)
        Server.consensusActor ! edge
      }

      // define new node
      val si = new SearchInterface(Server.store)
      val results = si.query(text.toLowerCase)
      val number = results.size + 1
      val newNode = Server.store.createTextNode(number.toString, text)

      // create revised edge
      val participantNodes = (for (pid <- participantIds) yield Server.store.get(pid)).toArray
      participantNodes(pos) = newNode
      Server.store.createAndConnectVertices2(rel, participantNodes, userNode.id)

      // force consesnsus re-evaluation of affected edge
      val edge = Edge(rel, participantNodes.map(_.id).toList)
      Server.consensusActor ! edge

      ResponseString(JSONGen.json(""))
    }
    case req@POST(Path("/disambig_change") & Params(params) & Cookies(cookies)) => {
      val userNode = Server.getUser(cookies)

      val mode = params("mode")(0)
      val rel = params("rel")(0)
      val participants = params("participants")(0)
      val pos = (params("pos")(0)).toInt
      val changeTo = params("changeto")(0)

      val participantIds = participants.split(" ").toList

      // undo previous connection
      if (mode == "change") {
        Server.store.delrel2(rel, participantIds, userNode.id)
        // force consesnsus re-evaluation of affected edge
        val edge = Edge(rel, participantIds)
        Server.consensusActor ! edge
      }

      // create revised edge
      val participantNodes = (for (pid <- participantIds) yield Server.store.get(pid)).toArray
      val newParticipantIds = participantIds.toArray
      newParticipantIds(pos) = changeTo
      Server.store.addrel2(rel, newParticipantIds, userNode.id)

      // force consesnsus re-evaluation of affected edge
      val edge = Edge(rel, newParticipantIds.toList)
      Server.consensusActor ! edge

      ResponseString(JSONGen.json(""))
    }
  }
}
