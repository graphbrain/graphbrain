package com.graphbrain.webapp


import unfiltered.scalate._
import unfiltered.request._
import unfiltered.response._

import com.graphbrain.gbdb.VertexStore
import com.graphbrain.gbdb.Vertex
import com.graphbrain.gbdb.Edge
import com.graphbrain.gbdb.UserNode


case class RawPage(vertex: Vertex, user: UserNode, req: HttpRequest[Any], cookies: Map[String, Any]) {
  var html = "<h2>Vertex: " + vertex.id + "</h2>"

  html += vertex.raw

  val edgeIds = Server.store.neighborEdges2(vertex.id, user.id)
  for (eid <- edgeIds)
    html += eid + "<br />"

  def response = Server.scalateResponse("raw.ssp", "raw", vertex.toString, cookies, req, html=html)
}

object RawPage {
}
