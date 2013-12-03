package com.graphbrain.webapp

import unfiltered.request._
import com.graphbrain.db.Vertex
import com.graphbrain.db.UserNode
import scala.collection.JavaConversions._

case class RawPage(vertex: Vertex, user: UserNode, req: HttpRequest[Any], cookies: Map[String, Any]) {
  var html = "<h2>Vertex: " + vertex.id + "</h2>"

  html += vertex.raw

  val userId = if (user == null) null else user.id
  val edgeIds = WebServer.graph.edges(vertex.id, userId)
  for (eid <- edgeIds)
    html += eid + "<br />"

  def response = WebServer.scalateResponse("raw.ssp", "raw", vertex.toString, cookies, req, html=html)
}

object RawPage {
}
