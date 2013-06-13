package com.graphbrain.webapp

import akka.actor.{Actor, Props}
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.{JsonContent, ResponseString}

import com.graphbrain.gbdb.Vertex
import com.graphbrain.gbdb.Edge
import com.graphbrain.gbdb.UserNode

import com.graphbrain.utils.SimpleLog


object RelationsResponderActor {
  case class Relation(edgeType: String, pos: Integer, root: Vertex, user: UserNode, responder: Async.Responder[HttpResponse])
}

class RelationsResponderActor() extends Actor with SimpleLog{
  import RelationsResponderActor._

  override protected def receive = {
    case Relation(edgeType, pos, root, user, responder) =>
      val reply = VisualGraph.generate(root.id, Server.store, user, edgeType, pos)
      responder.respond(JsonContent ~> ResponseString(reply))
  }
}
