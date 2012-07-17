package com.graphbrain.webapp


import unfiltered.scalate._
import unfiltered.request._
import unfiltered.response._

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.UserNode


case class RawPage(vertex: Vertex, req: HttpRequest[Any], cookies: Map[String, Any]) {
  var html = "<h2>Vertex: " + vertex.id + "</h2>"

  val fieldMap = vertex.toMap
  val hiddenKeys = Set("pwdhash", "email", "session", "edgesets")
  val safeMap = fieldMap.filterKeys(k => !(hiddenKeys contains k))
  html += (for (kv <- safeMap) yield "<strong>" + kv._1 + ":</strong> " + kv._2 + "<br />").reduceLeft(_ + "\n" + _)

  html += "<br /><h3>Edge Sets</h3>"
  for (es <- vertex.edgesets) {
    html += "<strong><a href='/raw/" + es + "'>" + es + "</a></strong><br />"
    val edgeSet = Server.store.getEdgeSet(es)
    for (e <- edgeSet.edges) {
      val parts = Edge.parts(e)
      for (p <- parts) {
        html += "<a href='/raw/" + p + "'>" + p + "</a> "
      }
      html += "<br />"
    }
  }

  def response = Server.scalateResponse("raw.ssp", "raw", cookies, req, html=html)
}

object RawPage {
}