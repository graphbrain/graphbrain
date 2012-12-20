package com.graphbrain.webapp


import unfiltered.scalate._
import unfiltered.request._
import unfiltered.response._

import com.graphbrain.hgdb.UserOps
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.UserNode


case class NodePage(store: UserOps, node: Vertex, user: UserNode, prod: Boolean, req: HttpRequest[Any], cookies: Map[String, Any], errorMsg: String="") {
  val userId = if (user == null) "" else user.id

  val js = "var data = " + VisualGraph.generate(node.id, store, user) + ";\n" +
    "var errorMsg = \"" + errorMsg + "\";\n"

  def response = Server.scalateResponse("node.ssp", "node", node.toString, cookies, req, js, "")
}

object NodePage {    
}
