package com.graphbrain.webapp

import akka.actor.Actor
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.Async
import unfiltered.response.{JsonContent, ResponseString}
import com.graphbrain.db.Vertex
import com.graphbrain.db.UserNode

object RelationsResponderActor {
  case class Relation(edgeType: String, pos: Integer, root: Vertex, user: UserNode, responder: Async.Responder[HttpResponse])
}

class RelationsResponderActor() extends Actor {
  import RelationsResponderActor._

  override def receive = {
    case Relation(edgeType, pos, root, user, responder) =>
      val reply = VisualGraph.generate(root.id, WebServer.graph, user, edgeType, pos)
      responder.respond(JsonContent ~> ResponseString(reply))
  }
}