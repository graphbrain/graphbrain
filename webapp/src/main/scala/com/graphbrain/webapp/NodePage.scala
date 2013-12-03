package com.graphbrain.webapp

import unfiltered.request._

import com.graphbrain.db.Graph
import com.graphbrain.db.Vertex
import com.graphbrain.db.UserNode


case class NodePage(store: Graph, node: Vertex, user: UserNode, req: HttpRequest[Any], cookies: Map[String, Any], errorMsg: String="") {
  val userId = if (user == null) "" else user.id

  val js = "var data = " + VisualGraph.generate(node.id, store, user) + ";\n" +
    "var errorMsg = \"" + errorMsg + "\";\n"

  def response = WebServer.scalateResponse("node.ssp", "node", node.toString, cookies, req, js, "")
}

object NodePage {    
}